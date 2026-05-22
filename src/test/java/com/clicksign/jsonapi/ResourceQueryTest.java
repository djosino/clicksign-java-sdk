package com.clicksign.jsonapi;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.types.DocumentStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

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
    static void stopWireMock() {
        wireMock.stop();
    }

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

    @Test
    void filterWithEnumUsesApiValue() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("filter[status]", equalTo("running"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.documentWithStatus("doc-1", "a.pdf", "running", ENVELOPE_ID)))));

        List<Document> docs = documentService.filter(ENVELOPE_ID)
            .filter("status", DocumentStatus.RUNNING)
            .fetch();

        assertEquals(DocumentStatus.RUNNING, docs.get(0).statusAsEnum());
    }

    @Test
    void duplicateFilterKeyOverwritesPreviousValue() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("filter[status]", equalTo("closed"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.documentWithStatus("doc-1", "a.pdf", "closed", ENVELOPE_ID)))));

        documentService.filter(ENVELOPE_ID)
            .filter("status", "draft")
            .filter("status", DocumentStatus.CLOSED)
            .fetch();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("filter[status]", equalTo("closed")));
    }

    @Test
    void filterRejectsBlankKey() {
        DocumentQuery query = documentService.filter(ENVELOPE_ID);
        assertThrows(IllegalArgumentException.class, () -> query.filter(" ", "x"));
    }

    @Test
    void filterRejectsNullValue() {
        DocumentQuery query = documentService.filter(ENVELOPE_ID);
        assertThrows(IllegalArgumentException.class, () -> query.filter("status", (String) null));
        assertThrows(IllegalArgumentException.class, () -> query.filter("status", (DocumentStatus) null));
    }

    @Test
    void includeMergesMultipleCalls() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("include", equalTo("envelope,signers"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.document("doc-1", "a.pdf", ENVELOPE_ID)))));

        documentService.filter(ENVELOPE_ID)
            .include("envelope")
            .include("signers")
            .fetch();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("include", equalTo("envelope,signers")));
    }

    @Test
    void fieldsAddsSparseFieldset() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("fields[documents]", equalTo("filename"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.document("doc-1", "a.pdf", ENVELOPE_ID)))));

        documentService.filter(ENVELOPE_ID).fields("documents", "filename").fetch();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("fields[documents]", equalTo("filename")));
    }
}
