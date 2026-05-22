package com.clicksign.instrumentation;

import com.clicksign.ClientConfig;
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.errors.ServerException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class InstrumentationTest {

    private static WireMockServer wireMock;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() { wireMock.stop(); }

    @BeforeEach
    void setUp() { wireMock.resetAll(); }

    private ClicksignClient.Builder clientBuilder() {
        return ClicksignClient.builder()
            .apiKey("test-token")
            .baseUrl("http://localhost:" + wireMock.port());
    }

    @Test
    void onRequestIsCalledOnSuccess() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(okJson("{\"data\":[]}")));

        List<RequestEvent> events = new ArrayList<>();
        ClicksignClient client = clientBuilder()
            .onRequest(events::add)
            .build();

        client.envelopes().list();

        assertEquals(1, events.size());
        assertEquals("get", events.get(0).method());
        assertEquals("/envelopes", events.get(0).path());
        assertEquals(200, events.get(0).status());
        assertEquals(1, events.get(0).attempt());
        assertTrue(events.get(0).durationMs() >= 0);
    }

    @Test
    void onErrorIsCalledOnHttpError() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(404)
                .withBody("{\"errors\":[{\"detail\":\"not found\"}]}")));

        List<ErrorEvent> errors = new ArrayList<>();
        ClicksignClient client = clientBuilder()
            .onError(errors::add)
            .build();

        assertThrows(Exception.class, () -> client.envelopes().list());

        assertEquals(1, errors.size());
        assertEquals(404, errors.get(0).status());
        assertEquals("get", errors.get(0).method());
    }

    @Test
    void onRetryIsCalledBeforeEachRetry() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .inScenario("retry")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(500).withBody("{\"errors\":[{\"detail\":\"err\"}]}"))
            .willSetStateTo("second"));

        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .inScenario("retry")
            .whenScenarioStateIs("second")
            .willReturn(okJson("{\"data\":[]}")));

        List<RetryEvent> retries = new ArrayList<>();
        List<RequestEvent> requests = new ArrayList<>();
        ClicksignClient client = clientBuilder()
            .maxRetries(1)
            .onRetry(retries::add)
            .onRequest(requests::add)
            .build();

        client.envelopes().list();

        assertEquals(1, retries.size());
        assertEquals(1, retries.get(0).attempt());
        assertEquals(1, retries.get(0).maxRetries());
        assertNotNull(retries.get(0).error());
        assertEquals(2, requests.size());
        assertEquals(500, requests.get(0).status());
        assertEquals(200, requests.get(1).status());
    }

    @Test
    void callbackExceptionDoesNotPropagateToRequest() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(okJson("{\"data\":[]}")));

        ClicksignClient client = clientBuilder()
            .onRequest(e -> { throw new RuntimeException("callback boom"); })
            .build();

        // Should not throw despite callback exception
        assertDoesNotThrow(() -> client.envelopes().list());
    }
}
