package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.ValidationException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class EnvelopeTest {

    private static WireMockServer wireMock;
    private Envelope.Service service;

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
        service = new Envelope.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsEnvelopes() {
        wireMock.stubFor(get(urlEqualTo("/envelopes"))
            .willReturn(okJson(JsonApiFixtures.envelopeList(
                JsonApiFixtures.envelope("env-1", "Contrato A", "draft"),
                JsonApiFixtures.envelope("env-2", "Contrato B", "running")
            ))));

        List<Envelope> envelopes = service.list();
        assertEquals(2, envelopes.size());
        assertEquals("env-1", envelopes.get(0).id());
        assertEquals("Contrato A", envelopes.get(0).name());
        assertEquals("draft", envelopes.get(0).status());
    }

    @Test
    void retrieveReturnsEnvelope() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/env-1"))
            .willReturn(okJson(JsonApiFixtures.envelope("env-1", "Contrato", "draft"))));

        Envelope envelope = service.retrieve("env-1");
        assertEquals("env-1", envelope.id());
        assertEquals("Contrato", envelope.name());
    }

    @Test
    void retrieveThrowsNotFoundError() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/bad-id"))
            .willReturn(aResponse().withStatus(404)
                .withBody(JsonApiFixtures.errorBody("not found"))));

        assertThrows(NotFoundException.class, () -> service.retrieve("bad-id"));
    }

    @Test
    void createReturnsNewEnvelope() {
        wireMock.stubFor(post(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.envelope("env-new", "Novo Contrato", "draft"))));

        Envelope envelope = service.create(
            Envelope.CreateParams.builder()
                .name("Novo Contrato")
                .locale("pt-BR")
                .build()
        );

        assertEquals("env-new", envelope.id());
        assertEquals("Novo Contrato", envelope.name());
    }

    @Test
    void createSendsCorrectBody() {
        wireMock.stubFor(post(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.envelope("env-1", "Test", "draft"))));

        service.create(Envelope.CreateParams.builder().name("Test").autoClose(true).build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes"))
            .withRequestBody(matchingJsonPath("$.data.type", equalTo("envelopes")))
            .withRequestBody(matchingJsonPath("$.data.attributes.name", equalTo("Test")))
            .withRequestBody(matchingJsonPath("$.data.attributes.auto_close", equalTo("true"))));
    }

    @Test
    void createThrowsValidationException() {
        wireMock.stubFor(post(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(422)
                .withBody(JsonApiFixtures.errorBody("name is blank"))));

        assertThrows(ValidationException.class,
            () -> service.create(Envelope.CreateParams.builder().name("x").build()));
    }

    @Test
    void createRequiresName() {
        assertThrows(IllegalArgumentException.class,
            () -> Envelope.CreateParams.builder().build());
    }

    @Test
    void updateReturnsUpdatedEnvelope() {
        wireMock.stubFor(patch(urlEqualTo("/envelopes/env-1"))
            .willReturn(okJson(JsonApiFixtures.envelope("env-1", "Nome Atualizado", "draft"))));

        Envelope updated = service.update("env-1",
            Envelope.UpdateParams.builder().name("Nome Atualizado").build());

        assertEquals("Nome Atualizado", updated.name());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/envelopes/env-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("env-1"));
    }

    @Test
    void activateReturnsEnvelopeWithRunningStatus() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/env-1/activate"))
            .willReturn(okJson(JsonApiFixtures.envelope("env-1", "Contrato", "running"))));

        Envelope activated = service.activate("env-1");
        assertEquals("running", activated.status());
    }

    @Test
    void activateSendsJsonApiBody() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/env-1/activate"))
            .willReturn(okJson(JsonApiFixtures.envelope("env-1", "Contrato", "running"))));

        service.activate("env-1");

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/env-1/activate"))
            .withRequestBody(matchingJsonPath("$.data.type", equalTo("envelopes")))
            .withRequestBody(matchingJsonPath("$.data.id", equalTo("env-1"))));
    }

    @Test
    void createSendsDeadlineFields() {
        wireMock.stubFor(post(urlEqualTo("/envelopes"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.envelope("env-1", "Contrato", "draft"))));

        String deadlineAt = OffsetDateTime.now().plusDays(30).toString();

        service.create(Envelope.CreateParams.builder()
            .name("Contrato")
            .deadlineAt(deadlineAt)
            .deadlinePartialSignatureAction("closed")
            .remindInterval(3)
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes"))
            .withRequestBody(matchingJsonPath("$.data.attributes.deadline_at",
                equalTo(deadlineAt)))
            .withRequestBody(matchingJsonPath("$.data.attributes.deadline_partial_signature_action",
                equalTo("closed")))
            .withRequestBody(matchingJsonPath("$.data.attributes.remind_interval", equalTo("3"))));
    }

    @Test
    void retrieveParsesDeadlineFields() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/env-1"))
            .willReturn(okJson(JsonApiFixtures.envelopeWithDeadlines(
                "env-1", "Contrato", "draft",
                "2026-08-19T23:59:59.000-03:00", "canceled", 7))));

        Envelope envelope = service.retrieve("env-1");

        assertEquals("2026-08-19T23:59:59.000-03:00", envelope.deadlineAt());
        assertEquals("canceled", envelope.deadlinePartialSignatureAction());
        assertEquals(Integer.valueOf(7), envelope.remindInterval());
    }

    @Test
    void notifyAllReturnsNotificationWithSummary() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/env-1/notifications"))
            .willReturn(okJson(JsonApiFixtures.envelopeNotification(
                "notif-1", "sig-1", "sig-2"))));

        Notification notification = service.notifyAll("env-1",
            NotificationParams.builder()
                .message("Assine o contrato")
                .build());

        assertEquals("notif-1", notification.id());
        assertEquals("Mensagem de teste", notification.message());
        assertEquals(2, notification.summary().size());
        assertEquals("sig-1", notification.summary().get(0).signerId());
        assertTrue(notification.summary().get(0).notified());
    }

    @Test
    void notifyAllSendsCorrectBody() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/env-1/notifications"))
            .willReturn(okJson(JsonApiFixtures.envelopeNotification("notif-1", "sig-1", "sig-2"))));

        service.notifyAll("env-1", NotificationParams.builder()
            .message("Olá")
            .emailCustomization(Map.of("subject", "Contrato pendente"))
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/env-1/notifications"))
            .withRequestBody(matchingJsonPath("$.data.type", equalTo("notifications")))
            .withRequestBody(matchingJsonPath("$.data.attributes.message", equalTo("Olá")))
            .withRequestBody(matchingJsonPath("$.data.attributes.email_customization.subject",
                equalTo("Contrato pendente"))));
    }

    @Test
    void updateSendsRemindIntervalMetadataAndDefaults() {
        wireMock.stubFor(patch(urlEqualTo("/envelopes/env-1"))
            .willReturn(okJson(JsonApiFixtures.envelope("env-1", "Contrato", "draft"))));

        service.update("env-1", Envelope.UpdateParams.builder()
            .remindInterval(7)
            .metadata(Map.of("contract_id", "12345"))
            .defaultSubject("Assinatura pendente")
            .defaultMessage("Por favor, assine o documento.")
            .build());

        wireMock.verify(patchRequestedFor(urlEqualTo("/envelopes/env-1"))
            .withRequestBody(matchingJsonPath("$.data.attributes.remind_interval", equalTo("7")))
            .withRequestBody(matchingJsonPath("$.data.attributes.metadata.contract_id", equalTo("12345")))
            .withRequestBody(matchingJsonPath("$.data.attributes.default_subject", equalTo("Assinatura pendente")))
            .withRequestBody(matchingJsonPath("$.data.attributes.default_message",
                equalTo("Por favor, assine o documento."))));
    }

    @Test
    void updateSendsDeadlineFields() {
        wireMock.stubFor(patch(urlEqualTo("/envelopes/env-1"))
            .willReturn(okJson(JsonApiFixtures.envelope("env-1", "Contrato", "draft"))));

        String deadlineAt = OffsetDateTime.now().plusDays(45).toString();

        service.update("env-1", Envelope.UpdateParams.builder()
            .deadlineAt(deadlineAt)
            .deadlinePartialSignatureAction("closed")
            .build());

        wireMock.verify(patchRequestedFor(urlEqualTo("/envelopes/env-1"))
            .withRequestBody(matchingJsonPath("$.data.attributes.deadline_at",
                equalTo(deadlineAt)))
            .withRequestBody(matchingJsonPath("$.data.attributes.deadline_partial_signature_action",
                equalTo("closed"))));
    }

    @Test
    void filterReturnsQuery() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes"))
            .withQueryParam("filter[status]", equalTo("draft"))
            .willReturn(okJson(JsonApiFixtures.envelopeList(
                JsonApiFixtures.envelope("env-1", "Draft", "draft")
            ))));

        List<Envelope> result = service.filter()
            .filter("status", "draft")
            .fetch();

        assertEquals(1, result.size());
        assertEquals("draft", result.get(0).status());
    }
}
