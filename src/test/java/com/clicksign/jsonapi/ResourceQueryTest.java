package com.clicksign.jsonapi;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.resources.notarial.Document;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ResourceQueryTest {

    private static final String ENVELOPE_ID = "env-1";

    private static WireMockServer wireMock;
    private Document.Service documentService;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() { wireMock.stop(); }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        String baseUrl = "http://localhost:" + wireMock.port();
        ClientConfig config = ClientConfig.builder().apiKey("test-token").baseUrl(baseUrl).build();
        HttpClient http = new HttpClient(config, new Instrumentation());
        documentService = new Document.Service(http);
    }

    @Test
    void fetchAllPaginatesWithNextLink() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("page[number]", equalTo("1"))
            .withQueryParam("page[size]", equalTo("2"))
            .willReturn(aResponse().withStatus(200)
                .withBody(JsonApiFixtures.documentListWithNext("http://next.example/page2",
                    JsonApiFixtures.document("doc-1", "a.pdf", ENVELOPE_ID)))));

        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("page[number]", equalTo("2"))
            .withQueryParam("page[size]", equalTo("2"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.document("doc-2", "b.pdf", ENVELOPE_ID)))));

        List<Document> all = documentService.filter(ENVELOPE_ID).perPage(2).fetchAll();

        assertEquals(2, all.size());
        assertEquals("doc-1", all.get(0).id());
        assertEquals("doc-2", all.get(1).id());
    }

    @Test
    void filterAppliesQueryParams() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .withQueryParam("filter[email]", equalTo("joao@example.com"))
            .willReturn(okJson(JsonApiFixtures.signerList(
                JsonApiFixtures.signer("sig-1", "João Silva", "joao@example.com", ENVELOPE_ID)))));

        ClientConfig config = ClientConfig.builder()
            .apiKey("test-token")
            .baseUrl("http://localhost:" + wireMock.port())
            .build();
        var signers = new com.clicksign.resources.notarial.Signer.Service(
            new HttpClient(config, new Instrumentation()));

        var result = signers.filter(ENVELOPE_ID).filter("email", "joao@example.com").fetch();
        assertEquals(1, result.size());
    }
}
