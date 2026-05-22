package com.clicksign.http;

import com.clicksign.ClientConfig;
import com.clicksign.errors.AuthenticationException;
import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.RateLimitException;
import com.clicksign.errors.ServerException;
import com.clicksign.errors.ServiceUnavailableException;
import com.clicksign.errors.ValidationException;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class HttpClientTest {

    private static WireMockServer wireMock;
    private static String baseUrl;
    private HttpClient client;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        baseUrl = "http://localhost:" + wireMock.port();
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
            .baseUrl(baseUrl)
            .build();
        client = new HttpClient(config, new Instrumentation());
    }

    @Test
    void getReturnsResponseBody() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withBody("{\"data\":[]}")));

        String result = client.get("/envelopes", Collections.emptyMap());
        assertEquals("{\"data\":[]}", result);
    }

    @Test
    void getSendsCorrectHeaders() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(200).withBody("{\"data\":[]}")));

        client.get("/envelopes", Collections.emptyMap());

        wireMock.verify(getRequestedFor(urlEqualTo("/envelopes"))
            .withHeader("Authorization",  equalTo("test-token"))
            .withHeader("Content-Type",   equalTo("application/vnd.api+json"))
            .withHeader("Accept",         equalTo("application/vnd.api+json")));
    }

    @Test
    void getEncodesQueryParams() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes"))
            .withQueryParam("filter[status]", equalTo("running"))
            .willReturn(aResponse().withStatus(200).withBody("{\"data\":[]}")));

        client.get("/envelopes", Collections.singletonMap("filter[status]", "running"));

        wireMock.verify(getRequestedFor(urlPathEqualTo("/envelopes"))
            .withQueryParam("filter[status]", equalTo("running")));
    }

    @Test
    void putReturnsResponseBody() {
        wireMock.stubFor(put(urlEqualTo("/memberships/mem-1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/vnd.api+json")
                .withBody("{\"data\":{\"id\":\"mem-1\",\"type\":\"memberships\",\"attributes\":{\"role\":\"admin\"}}}")));

        String result = client.put("/memberships/mem-1",
            "{\"data\":{\"type\":\"memberships\",\"id\":\"mem-1\",\"attributes\":{\"role\":\"admin\"}}}");
        assertTrue(result.contains("mem-1"));
    }

    @Test
    void deleteReturnsNullOnEmptyBody() {
        wireMock.stubFor(delete(urlEqualTo("/envelopes/1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> client.delete("/envelopes/1", null));
    }

    @Test
    void throws401AsAuthenticationException() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(401)
                .withBody("{\"errors\":[{\"detail\":\"invalid token\"}]}")));

        assertThrows(AuthenticationException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
    }

    @Test
    void throws404AsNotFoundException() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/x"))
            .willReturn(aResponse().withStatus(404)
                .withBody("{\"errors\":[{\"detail\":\"not found\"}]}")));

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> client.get("/envelopes/x", Collections.emptyMap()));
        assertEquals(404, ex.statusCode());
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void throws422AsValidationException() {
        wireMock.stubFor(post(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"errors\":[{\"detail\":\"name is blank\"}]}")));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> client.post("/envelopes", "{\"data\":{\"type\":\"envelopes\",\"attributes\":{}}}"));
        assertFalse(ex.isRetryable());
    }

    @Test
    void throws500AsServerException() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(500)
                .withBody("{\"errors\":[{\"detail\":\"server error\"}]}")));

        ServerException ex = assertThrows(ServerException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
        assertTrue(ex.isRetryable());
    }

    @Test
    void throws429AsRateLimitException() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(429)
                .withBody("{\"errors\":[{\"detail\":\"rate limit exceeded\"}]}")));

        RateLimitException ex = assertThrows(RateLimitException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
        assertTrue(ex.isRetryable());
    }

    @Test
    void throws503AsServiceUnavailableException() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(503)
                .withHeader("Retry-After", "2")
                .withBody("{\"errors\":[{\"detail\":\"service unavailable\"}]}")));

        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
        assertTrue(ex.isRetryable());
        assertEquals(Long.valueOf(2), ex.retryAfterSeconds());
        assertEquals("service unavailable", ex.getMessage());
    }

    @Test
    void rateLimitExceptionExposesRetryAfter() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "5")
                .withBody("{\"errors\":[{\"detail\":\"too many requests\"}]}")));

        RateLimitException ex = assertThrows(RateLimitException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
        assertEquals(Long.valueOf(5), ex.retryAfterSeconds());
    }

    @Test
    void exceptionExposeRequestId() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(404)
                .withHeader("x-request-id", "req-abc")
                .withBody("{\"errors\":[{\"detail\":\"not found\"}]}")));

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
        assertEquals("req-abc", ex.requestId());
    }

    @Test
    void retriesOnServerErrorAndSucceeds() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .inScenario("retry")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(500).withBody("{\"errors\":[{\"detail\":\"err\"}]}"))
            .willSetStateTo("second"));

        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .inScenario("retry")
            .whenScenarioStateIs("second")
            .willReturn(aResponse().withStatus(200).withBody("{\"data\":[]}")));

        ClientConfig config = ClientConfig.builder()
            .apiKey("test-token").baseUrl(baseUrl).maxRetries(1).build();
        HttpClient retryClient = new HttpClient(config, new Instrumentation());

        String result = retryClient.get("/envelopes", Collections.emptyMap());
        assertEquals("{\"data\":[]}", result);
    }

    @Test
    void doesNotRetryValidationError() {
        wireMock.stubFor(post(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"errors\":[{\"detail\":\"invalid\"}]}")));

        ClientConfig config = ClientConfig.builder()
            .apiKey("test-token").baseUrl(baseUrl).maxRetries(3).build();
        HttpClient retryClient = new HttpClient(config, new Instrumentation());

        assertThrows(ValidationException.class,
            () -> retryClient.post("/envelopes", "{}"));
        wireMock.verify(1, postRequestedFor(urlEqualTo("/envelopes")));
    }

    @Test
    void fallsBackToResponseMessageWhenErrorsHaveNoDetail() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"errors\":[{}]}")));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> client.get("/envelopes", Collections.emptyMap()));
        // Message should not be empty — falls back to response string
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isEmpty());
    }
}
