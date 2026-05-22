package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.resources.types.EventCustomKind;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private static final String ENVELOPE_ID = "env-1";

    private static WireMockServer wireMock;
    private Event.Service service;

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
        service = new Event.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listForEnvelopeReturnsEvents() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/events"))
            .willReturn(okJson(JsonApiFixtures.eventList(
                JsonApiFixtures.event("evt-1", "sign"),
                JsonApiFixtures.event("evt-2", "close")
            ))));

        List<Event> events = service.listForEnvelope(ENVELOPE_ID);
        assertEquals(2, events.size());
        assertEquals("evt-1", events.get(0).id());
        assertEquals("sign", events.get(0).name());
    }

    @Test
    void createAddImagePostsToDocumentEvents() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1/events"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.event("evt-img", "add_image"))));

        Event event = service.createAddImage(Event.AddImageParams.builder()
            .envelopeId(ENVELOPE_ID)
            .documentId("doc-1")
            .contentBase64("aGVsbG8=")
            .title("Assinatura")
            .occurredAt("2026-01-01T12:00:00.000-03:00")
            .build());

        assertEquals("add_image", event.name());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1/events"))
            .withRequestBody(matchingJsonPath("$.data.attributes.name", equalTo("add_image")))
            .withRequestBody(matchingJsonPath("$.data.attributes.content_base64", equalTo("aGVsbG8="))));
    }

    @Test
    void createCustomPostsTokenEmail() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1/events"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.event("evt-custom", "custom"))));

        Event event = service.createCustom(Event.CustomParams.builder()
            .envelopeId(ENVELOPE_ID)
            .documentId("doc-1")
            .kind(EventCustomKind.TOKEN_EMAIL)
            .occurredAt("2026-01-01T12:00:00.000-03:00")
            .signerName("João Silva")
            .signerEmail("joao@example.com")
            .build());

        assertEquals("custom", event.name());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1/events"))
            .withRequestBody(matchingJsonPath("$.data.attributes.name", equalTo("custom")))
            .withRequestBody(matchingJsonPath("$.data.attributes.data.kind", equalTo("token_email"))));
    }

    @Test
    void createCustomRejectsInvalidKind() {
        Event.CustomParams params = Event.CustomParams.builder()
            .envelopeId(ENVELOPE_ID)
            .documentId("doc-1")
            .kind("invalid_kind")
            .occurredAt("2026-01-01T12:00:00.000-03:00")
            .signerName("João Silva")
            .build();

        assertThrows(IllegalArgumentException.class, () -> service.createCustom(params));
    }
}
