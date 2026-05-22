package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.ValidationException;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class DocumentTest {

    private static final String ENVELOPE_ID = "env-1";

    private static WireMockServer wireMock;
    private Document.Service service;

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
        service = new Document.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsDocuments() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.document("doc-1", "contrato.pdf", ENVELOPE_ID),
                JsonApiFixtures.document("doc-2", "anexo.pdf", ENVELOPE_ID)
            ))));

        List<Document> docs = service.list(ENVELOPE_ID);
        assertEquals(2, docs.size());
        assertEquals("doc-1", docs.get(0).id());
        assertEquals("contrato.pdf", docs.get(0).filename());
        assertEquals(ENVELOPE_ID, docs.get(0).envelopeId());
    }

    @Test
    void createWithTemplateSendsCorrectBody() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.document("doc-tpl", "modelo.docx", ENVELOPE_ID))));

        service.create(Document.CreateParams.builder()
            .envelopeId(ENVELOPE_ID)
            .filename("modelo.docx")
            .template(Map.of("id", "tpl-1", "campo1", "valor"))
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withRequestBody(matchingJsonPath("$.data.attributes.filename", equalTo("modelo.docx")))
            .withRequestBody(matchingJsonPath("$.data.attributes.template.id", equalTo("tpl-1")))
            .withRequestBody(matchingJsonPath("$.data.attributes.template.campo1", equalTo("valor"))));
    }

    @Test
    void createWithDuplicateSendsCorrectBody() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.document("doc-dup", "copia.pdf", ENVELOPE_ID))));

        service.create(Document.CreateParams.builder()
            .envelopeId(ENVELOPE_ID)
            .filename("copia.pdf")
            .duplicate(Map.of("id", "doc-original"))
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withRequestBody(matchingJsonPath("$.data.attributes.duplicate.id", equalTo("doc-original"))));
    }

    @Test
    void createWithTemplateStillRequiresFilename() {
        assertThrows(IllegalArgumentException.class, () ->
            Document.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .template(Map.of("id", "tpl-1"))
                .build());
    }

    @Test
    void retrieveReturnsDocument() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1"))
            .willReturn(okJson(JsonApiFixtures.document("doc-1", "contrato.pdf", ENVELOPE_ID))));

        Document doc = service.retrieve("doc-1", ENVELOPE_ID);
        assertEquals("doc-1", doc.id());
        assertEquals("contrato.pdf", doc.filename());
    }

    @Test
    void retrieveThrowsNotFound() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/missing"))
            .willReturn(aResponse().withStatus(404)
                .withBody(JsonApiFixtures.errorBody("document not found"))));

        assertThrows(NotFoundException.class, () -> service.retrieve("missing", ENVELOPE_ID));
    }

    @Test
    void updateReturnsDocument() {
        wireMock.stubFor(patch(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1"))
            .willReturn(okJson(JsonApiFixtures.document("doc-1", "renomeado.pdf", ENVELOPE_ID))));

        Document updated = service.update("doc-1", ENVELOPE_ID,
            Document.UpdateParams.builder().filename("renomeado.pdf").build());

        assertEquals("renomeado.pdf", updated.filename());
    }

    @Test
    void createThrowsValidationException() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .willReturn(aResponse().withStatus(422)
                .withBody(JsonApiFixtures.errorBody("invalid content"))));

        assertThrows(ValidationException.class, () ->
            service.create(Document.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .filename("bad.pdf")
                .contentBase64("!!!")
                .build()));
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("doc-1", ENVELOPE_ID));
    }

    @Test
    void listEventsReturnsEvents() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/documents/doc-1/events"))
            .willReturn(okJson(JsonApiFixtures.eventList(
                JsonApiFixtures.event("evt-1", "add_image"),
                JsonApiFixtures.event("evt-2", "custom")
            ))));

        List<Event> events = service.listEvents("doc-1", ENVELOPE_ID);
        assertEquals(2, events.size());
        assertEquals("add_image", events.get(0).name());
    }
}
