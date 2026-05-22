package com.clicksign.jsonapi;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.EnvelopeStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class EnvelopeQueryTest {

    private static WireMockServer wireMock;
    private com.clicksign.resources.notarial.Envelope.Service service;

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
        service = new com.clicksign.resources.notarial.Envelope.Service(
            new HttpClient(config, new Instrumentation()));
    }

    @Test
    void combinesStatusNameAndOrder() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes"))
            .withQueryParam("filter[status]", equalTo("running"))
            .withQueryParam("filter[name]", equalTo("Contrato Q1"))
            .withQueryParam("sort", equalTo("-name"))
            .willReturn(okJson(JsonApiFixtures.envelopeList(
                JsonApiFixtures.envelope("env-1", "Contrato Q1", "running")))));

        List<Envelope> result = service.filter()
            .status(EnvelopeStatus.RUNNING)
            .name("Contrato Q1")
            .orderByName(true)
            .fetch();

        assertEquals(1, result.size());
        assertEquals(EnvelopeStatus.RUNNING, result.get(0).statusAsEnum());
    }

    @Test
    void appliesDateFiltersAndAscendingNameOrder() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes"))
            .withQueryParam("filter[created]", equalTo("2026-01-01,2026-12-31"))
            .withQueryParam("filter[modified]", equalTo("2026-06-01,2026-06-30"))
            .withQueryParam("filter[deadline_at]", equalTo("2026-07-01,2026-07-31"))
            .withQueryParam("sort", equalTo("name"))
            .willReturn(okJson(JsonApiFixtures.envelopeList(
                JsonApiFixtures.envelope("env-1", "A", "draft")))));

        service.filter()
            .created("2026-01-01,2026-12-31")
            .modified("2026-06-01,2026-06-30")
            .deadlineAt("2026-07-01,2026-07-31")
            .orderByName(false)
            .fetch();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/envelopes"))
            .withQueryParam("sort", equalTo("name")));
    }

    @Test
    void chainsPaginationAfterTypedFilters() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes"))
            .withQueryParam("filter[status]", equalTo("draft"))
            .withQueryParam("page[number]", equalTo("2"))
            .withQueryParam("page[size]", equalTo("10"))
            .willReturn(okJson(JsonApiFixtures.envelopeList(
                JsonApiFixtures.envelope("env-2", "Página 2", "draft")))));

        List<Envelope> page = service.filter()
            .status(EnvelopeStatus.DRAFT)
            .page(2)
            .perPage(10)
            .fetch();

        assertEquals(1, page.size());
        assertEquals("env-2", page.get(0).id());
    }
}
