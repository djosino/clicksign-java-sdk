package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class EnvelopeBulkCreationTest {

    private static WireMockServer wireMock;
    private EnvelopeBulkCreation.Service service;

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
        ClientConfig config = ClientConfig.builder()
            .apiKey("test-token")
            .baseUrl("http://localhost:" + wireMock.port())
            .build();
        service = new EnvelopeBulkCreation.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void createReturnsBulkCreationJob() {
        wireMock.stubFor(post(urlEqualTo("/envelope_bulk_creations"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.envelopeBulkCreation("bulk-1", "job-abc"))));

        EnvelopeBulkCreation job = service.create(EnvelopeBulkCreation.CreateParams.builder()
            .envelope(Map.of("name", "Lote 1"))
            .document(Map.of("filename", "contrato.pdf"))
            .signers(List.of(Map.of("name", "João Silva", "email", "joao@example.com")))
            .build());

        assertEquals("bulk-1", job.id());
        assertEquals("job-abc", job.jobId());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelope_bulk_creations"))
            .withRequestBody(matchingJsonPath("$.data.attributes.envelope.name", equalTo("Lote 1")))
            .withRequestBody(matchingJsonPath("$.data.attributes.signers[0].email", equalTo("joao@example.com"))));
    }

    @Test
    void createRequiresEnvelopeDocumentAndSigners() {
        assertThrows(IllegalArgumentException.class, () ->
            EnvelopeBulkCreation.CreateParams.builder().build());
    }
}
