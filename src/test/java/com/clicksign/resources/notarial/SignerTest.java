package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.ValidationException;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class SignerTest {

    private static final String ENVELOPE_ID = "env-1";

    private static WireMockServer wireMock;
    private Signer.Service service;

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
        service = new Signer.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsSigners() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .willReturn(okJson(JsonApiFixtures.signerList(
                JsonApiFixtures.signer("sig-1", "João Silva", "joao@example.com", ENVELOPE_ID),
                JsonApiFixtures.signer("sig-2", "Maria Santos", "maria@example.com", ENVELOPE_ID)
            ))));

        List<Signer> signers = service.list(ENVELOPE_ID);
        assertEquals(2, signers.size());
        assertEquals("sig-1", signers.get(0).id());
        assertEquals("João Silva", signers.get(0).name());
    }

    @Test
    void retrieveParsesCommunicateEventsAndSignatureHost() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers/sig-1"))
            .willReturn(okJson(JsonApiFixtures.signerWithCommunicateEvents(
                "sig-1", "João Silva", "joao@example.com", ENVELOPE_ID))));

        Signer signer = service.retrieve("sig-1", ENVELOPE_ID);

        assertEquals(Integer.valueOf(1), signer.group());
        assertEquals("email", signer.communicateEvents().get("signature_request"));
        assertEquals("whatsapp", signer.communicateEvents().get("document_signed"));
        assertNotNull(signer.signatureHost());
        assertEquals("Host Name", signer.signatureHost().name());
        assertEquals("host@example.com", signer.signatureHost().email());
        assertEquals("email", signer.signatureHost().communicateEvents().get("signature_request"));
    }

    @Test
    void createSendsCommunicateEventsAndSignatureHost() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.signer("sig-new", "João Silva", "joao@example.com", ENVELOPE_ID))));

        Map<String, Object> communicateEvents = Map.of(
            "signature_request", "email",
            "signature_reminder", "none",
            "document_signed", "whatsapp"
        );
        Signer.SignatureHost host = new Signer.SignatureHost(
            "Host Name", "host@example.com", Map.of("signature_request", "email"));

        service.create(Signer.CreateParams.builder()
            .envelopeId(ENVELOPE_ID)
            .name("João Silva")
            .email("joao@example.com")
            .phoneNumber("11987654321")
            .communicateEvents(communicateEvents)
            .signatureHost(host)
            .group(2)
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .withRequestBody(matchingJsonPath("$.data.attributes.communicate_events.signature_request", equalTo("email")))
            .withRequestBody(matchingJsonPath("$.data.attributes.signature_host.name", equalTo("Host Name")))
            .withRequestBody(matchingJsonPath("$.data.attributes.group", equalTo("2"))));
    }

    @Test
    void createRejectsNameWithoutLastName() {
        assertThrows(IllegalArgumentException.class, () ->
            Signer.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .name("João")
                .email("joao@example.com")
                .build());
    }

    @Test
    void createRejectsNameWithNumbers() {
        assertThrows(IllegalArgumentException.class, () ->
            Signer.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .name("João Silva 123")
                .email("joao@example.com")
                .build());
    }

    @Test
    void createRejectsDocumentationWhenHasDocumentationFalse() {
        assertThrows(IllegalArgumentException.class, () ->
            Signer.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .name("João Silva")
                .email("joao@example.com")
                .hasDocumentation(false)
                .documentation("123.321.123-40")
                .build());
    }

    @Test
    void createRejectsInvalidBirthdayFormat() {
        assertThrows(IllegalArgumentException.class, () ->
            Signer.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .name("João Silva")
                .email("joao@example.com")
                .birthday("31/03/1983")
                .build());
    }

    @Test
    void createRequiresPhoneForWhatsapp() {
        assertThrows(IllegalArgumentException.class, () ->
            Signer.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .name("João Silva")
                .email("joao@example.com")
                .communicateEvents(Map.of("signature_request", "whatsapp"))
                .build());
    }

    @Test
    void createSendsHasDocumentationAndDocumentation() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.signer("sig-new", "João Silva", "joao@example.com", ENVELOPE_ID))));

        service.create(Signer.CreateParams.builder()
            .envelopeId(ENVELOPE_ID)
            .name("João Silva")
            .email("joao@example.com")
            .hasDocumentation(true)
            .documentation("123.321.123-40")
            .birthday("1983-03-31")
            .phoneNumber("11987654321")
            .communicateEvents(Map.of("signature_request", "whatsapp"))
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .withRequestBody(matchingJsonPath("$.data.attributes.has_documentation", equalTo("true")))
            .withRequestBody(matchingJsonPath("$.data.attributes.documentation", equalTo("123.321.123-40")))
            .withRequestBody(matchingJsonPath("$.data.attributes.birthday", equalTo("1983-03-31"))));
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers/sig-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("sig-1", ENVELOPE_ID));
    }

    @Test
    void notifySendsNotificationBody() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers/sig-1/notifications"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.envelopeNotification("notif-1", "sig-1", "sig-1"))));

        assertDoesNotThrow(() -> service.notify("sig-1", ENVELOPE_ID,
            NotificationParams.builder().message("Por favor, assine.").build()));

        wireMock.verify(postRequestedFor(
                urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers/sig-1/notifications"))
            .withRequestBody(matchingJsonPath("$.data.attributes.message", equalTo("Por favor, assine."))));
    }

    @Test
    void retrieveThrowsNotFound() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers/missing"))
            .willReturn(aResponse().withStatus(404)
                .withBody(JsonApiFixtures.errorBody("signer not found"))));

        assertThrows(NotFoundException.class, () -> service.retrieve("missing", ENVELOPE_ID));
    }

    @Test
    void createThrowsValidationException() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .willReturn(aResponse().withStatus(422)
                .withBody(JsonApiFixtures.errorBody("invalid email"))));

        assertThrows(ValidationException.class, () ->
            service.create(Signer.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .name("João Silva")
                .email("invalid")
                .build()));
    }
}

