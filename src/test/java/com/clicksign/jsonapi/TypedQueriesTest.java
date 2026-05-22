package com.clicksign.jsonapi;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.resources.AcceptanceTermWhatsapp;
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.notarial.Requirement;
import com.clicksign.resources.notarial.Signer;
import com.clicksign.resources.types.AcceptanceTermStatus;
import com.clicksign.resources.types.DocumentStatus;
import com.clicksign.resources.types.RequirementAction;
import com.clicksign.resources.types.RequirementRole;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

/** WireMock coverage for typed {@link TypedResourceQuery} subclasses. */
class TypedQueriesTest {

    private static final String ENVELOPE_ID = "env-typed-1";

    private static WireMockServer wireMock;
    private HttpClient http;

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
        http = new HttpClient(config, new Instrumentation());
    }

    @Test
    void documentQueryCombinesStatusFilenameIncludeAndFields() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/documents"))
            .withQueryParam("filter[status]", equalTo("draft"))
            .withQueryParam("filter[filename]", equalTo("contrato.pdf"))
            .withQueryParam("include", equalTo("envelope"))
            .withQueryParam("fields[documents]", equalTo("filename,status"))
            .willReturn(okJson(JsonApiFixtures.documentList(
                JsonApiFixtures.documentWithStatus("doc-1", "contrato.pdf", "draft", ENVELOPE_ID)))));

        List<Document> docs = new com.clicksign.resources.notarial.Document.Service(http)
            .filter(ENVELOPE_ID)
            .status(DocumentStatus.DRAFT)
            .filename("contrato.pdf")
            .include("envelope")
            .fields("documents", "filename", "status")
            .fetch();

        assertEquals(1, docs.size());
        assertEquals(DocumentStatus.DRAFT, docs.get(0).statusAsEnum());
        assertEquals("contrato.pdf", docs.get(0).filename());
    }

    @Test
    void signerQueryCombinesNameAndEmail() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/signers"))
            .withQueryParam("filter[name]", equalTo("João Silva"))
            .withQueryParam("filter[email]", equalTo("joao@example.com"))
            .withQueryParam("sort", equalTo("name"))
            .willReturn(okJson(JsonApiFixtures.signerList(
                JsonApiFixtures.signer("sig-1", "João Silva", "joao@example.com", ENVELOPE_ID)))));

        List<Signer> signers = new com.clicksign.resources.notarial.Signer.Service(http)
            .filter(ENVELOPE_ID)
            .name("João Silva")
            .email("joao@example.com")
            .order("name")
            .fetch();

        assertEquals(1, signers.size());
        assertEquals("joao@example.com", signers.get(0).email());
    }

    @Test
    void requirementQueryCombinesActionAndRole() {
        wireMock.stubFor(get(urlPathEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements"))
            .withQueryParam("filter[action]", equalTo("agree"))
            .withQueryParam("filter[role]", equalTo("sign"))
            .willReturn(okJson(JsonApiFixtures.requirementList(
                JsonApiFixtures.requirementWithRole("req-1", "agree", "sign", ENVELOPE_ID)))));

        List<Requirement> requirements = new com.clicksign.resources.notarial.Requirement.Service(http)
            .filter(ENVELOPE_ID)
            .action(RequirementAction.AGREE)
            .role(RequirementRole.SIGN)
            .fetch();

        assertEquals(1, requirements.size());
        assertEquals(RequirementAction.AGREE, requirements.get(0).actionAsEnum());
        assertEquals(RequirementRole.SIGN, requirements.get(0).roleAsEnum());
    }

    @Test
    void acceptanceTermWhatsappQueryFiltersByStatusEnum() {
        wireMock.stubFor(get(urlPathEqualTo("/acceptance_term/whatsapps"))
            .withQueryParam("filter[status]", equalTo("sent"))
            .willReturn(okJson(JsonApiFixtures.acceptanceTermWhatsappList(
                JsonApiFixtures.acceptanceTermWhatsapp("wa-1", "sent", "Contrato")))));

        List<AcceptanceTermWhatsapp> result = new com.clicksign.resources.AcceptanceTermWhatsapp.Service(http)
            .filter()
            .status(AcceptanceTermStatus.SENT)
            .fetch();

        assertEquals(1, result.size());
        assertEquals(AcceptanceTermStatus.SENT, result.get(0).statusAsEnum());
    }
}
