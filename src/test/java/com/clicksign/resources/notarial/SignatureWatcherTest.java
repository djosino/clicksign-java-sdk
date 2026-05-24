package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;
import java.util.Map;

import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.ValidationException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class SignatureWatcherTest {

    private static final String ENVELOPE_ID = "env-1";

    private static WireMockServer wireMock;
    private SignatureWatcher.Service service;

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
        service = new SignatureWatcher.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsSignatureWatchers() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signature_watchers"))
            .willReturn(okJson(JsonApiFixtures.signatureWatcherList(
                JsonApiFixtures.signatureWatcher("w-1", "watcher@example.com", ENVELOPE_ID)
            ))));

        List<SignatureWatcher> watchers = service.list(ENVELOPE_ID);
        assertEquals(1, watchers.size());
        assertEquals("w-1", watchers.get(0).id());
        assertEquals("watcher@example.com", watchers.get(0).email());
        assertEquals("email", watchers.get(0).communicateEvents().get("signature_watcher_document_sent"));
    }

    @Test
    void createRequiresEnvelopeId() {
        assertThrows(IllegalArgumentException.class, () ->
            SignatureWatcher.CreateParams.builder()
                .email("w@example.com")
                .kind("all_steps")
                .build());
    }

    @Test
    void createRequiresEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            SignatureWatcher.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .kind("all_steps")
                .build());
    }

    @Test
    void createRequiresKind() {
        assertThrows(IllegalArgumentException.class, () ->
            SignatureWatcher.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .email("w@example.com")
                .build());
    }

    @Test
    void createThrowsValidationException() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signature_watchers"))
            .willReturn(aResponse().withStatus(422)
                .withBody(JsonApiFixtures.errorBody("invalid email"))));

        assertThrows(ValidationException.class, () ->
            service.create(SignatureWatcher.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .email("bad-email")
                .kind("all_steps")
                .build()));
    }

    @Test
    void listThrowsNotFoundWhenEnvelopeMissing() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/missing/signature_watchers"))
            .willReturn(aResponse().withStatus(404)
                .withBody(JsonApiFixtures.errorBody("envelope not found"))));

        assertThrows(NotFoundException.class, () -> service.list("missing"));
    }

    @Test
    void createSendsCommunicateEvents() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signature_watchers"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.signatureWatcher("w-new", "new@example.com", ENVELOPE_ID))));

        service.create(SignatureWatcher.CreateParams.builder()
            .envelopeId(ENVELOPE_ID)
            .email("new@example.com")
            .kind("all_steps")
            .communicateEvents(Map.of("signature_watcher_document_sent", "email"))
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signature_watchers"))
            .withRequestBody(matchingJsonPath(
                "$.data.attributes.communicate_events.signature_watcher_document_sent", equalTo("email"))));
    }
}
