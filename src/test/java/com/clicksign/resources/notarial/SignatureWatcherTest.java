package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

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
    static void stopWireMock() { wireMock.stop(); }

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
