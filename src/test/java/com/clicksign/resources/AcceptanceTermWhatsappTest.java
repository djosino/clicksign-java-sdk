package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class AcceptanceTermWhatsappTest {

    private static WireMockServer wireMock;
    private AcceptanceTermWhatsapp.Service service;

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
        ClientConfig config = ClientConfig.builder()
            .apiKey("test-token")
            .baseUrl("http://localhost:" + wireMock.port())
            .build();
        service = new AcceptanceTermWhatsapp.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsAcceptances() {
        wireMock.stubFor(get(urlEqualTo("/acceptance_term/whatsapps"))
            .willReturn(okJson(JsonApiFixtures.acceptanceTermWhatsappList(
                JsonApiFixtures.acceptanceTermWhatsapp("wa-1", "sent", "Contrato A"),
                JsonApiFixtures.acceptanceTermWhatsapp("wa-2", "completed", "Contrato B")
            ))));

        List<AcceptanceTermWhatsapp> items = service.list();
        assertEquals(2, items.size());
        assertEquals("sent", items.get(0).status());
    }

    @Test
    void retrieveReturnsAcceptance() {
        wireMock.stubFor(get(urlEqualTo("/acceptance_term/whatsapps/wa-1"))
            .willReturn(okJson(JsonApiFixtures.acceptanceTermWhatsapp("wa-1", "enqueued", "Contrato"))));

        assertEquals("wa-1", service.retrieve("wa-1").id());
    }

    @Test
    void createSendsRequiredFields() {
        wireMock.stubFor(post(urlEqualTo("/acceptance_term/whatsapps"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.acceptanceTermWhatsapp("wa-new", "enqueued", "Novo"))));

        service.create(AcceptanceTermWhatsapp.CreateParams.builder()
            .title("Novo")
            .senderNameOption("account_name")
            .message("Leia e aceite o termo.")
            .signerPhone("11987654321")
            .signerName("João Silva")
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/acceptance_term/whatsapps"))
            .withRequestBody(matchingJsonPath("$.data.attributes.title", equalTo("Novo")))
            .withRequestBody(matchingJsonPath("$.data.attributes.signer_phone", equalTo("11987654321"))));
    }

    @Test
    void cancelSetsStatusCanceled() {
        wireMock.stubFor(patch(urlEqualTo("/acceptance_term/whatsapps/wa-1"))
            .willReturn(okJson(JsonApiFixtures.acceptanceTermWhatsapp("wa-1", "canceled", "Contrato"))));

        AcceptanceTermWhatsapp canceled = service.cancel("wa-1");
        assertEquals("canceled", canceled.status());

        wireMock.verify(patchRequestedFor(urlEqualTo("/acceptance_term/whatsapps/wa-1"))
            .withRequestBody(matchingJsonPath("$.data.attributes.status", equalTo("canceled"))));
    }

    @Test
    void filterByStatus() {
        wireMock.stubFor(get(urlPathEqualTo("/acceptance_term/whatsapps"))
            .withQueryParam("filter[status]", equalTo("sent"))
            .willReturn(okJson(JsonApiFixtures.acceptanceTermWhatsappList(
                JsonApiFixtures.acceptanceTermWhatsapp("wa-1", "sent", "Contrato")
            ))));

        List<AcceptanceTermWhatsapp> result = service.filter().filter("status", "sent").fetch();
        assertEquals(1, result.size());
        assertEquals("sent", result.get(0).status());
    }
}
