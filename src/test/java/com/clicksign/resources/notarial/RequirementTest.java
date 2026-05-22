package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

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

class RequirementTest {

    private static final String ENVELOPE_ID = "env-1";

    private static WireMockServer wireMock;
    private Requirement.Service service;

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
        service = new Requirement.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsRequirements() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements"))
            .willReturn(okJson(JsonApiFixtures.requirementList(
                JsonApiFixtures.requirement("req-1", "agree", ENVELOPE_ID),
                JsonApiFixtures.requirement("req-2", "provide_evidence", ENVELOPE_ID)
            ))));

        List<Requirement> requirements = service.list(ENVELOPE_ID);
        assertEquals(2, requirements.size());
        assertEquals("req-1", requirements.get(0).id());
        assertEquals("agree", requirements.get(0).action());
        assertEquals(ENVELOPE_ID, requirements.get(0).envelopeId());
    }

    @Test
    void createRubricateRequiresPagesOrRubricField() {
        assertThrows(IllegalArgumentException.class, () ->
            Requirement.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .action("rubricate")
                .build());
    }

    @Test
    void createRubricateAcceptsPages() {
        assertDoesNotThrow(() ->
            Requirement.CreateParams.builder()
                .envelopeId(ENVELOPE_ID)
                .action("rubricate")
                .pages("1-3")
                .build());
    }

    @Test
    void retrieveReturnsRequirement() {
        wireMock.stubFor(get(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements/req-1"))
            .willReturn(okJson(JsonApiFixtures.requirement("req-1", "agree", ENVELOPE_ID))));

        Requirement req = service.retrieve("req-1", ENVELOPE_ID);
        assertEquals("req-1", req.id());
        assertEquals("agree", req.action());
    }

    @Test
    void createSendsRelationships() {
        wireMock.stubFor(post(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.requirementWithRelations(
                    "req-new", "agree", ENVELOPE_ID, "doc-1", "sig-1"))));

        Requirement created = service.create(Requirement.CreateParams.builder()
            .envelopeId(ENVELOPE_ID)
            .action("agree")
            .role("sign")
            .documentId("doc-1")
            .signerId("sig-1")
            .build());

        assertEquals("req-new", created.id());

        wireMock.verify(postRequestedFor(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements"))
            .withRequestBody(matchingJsonPath("$.data.relationships.document.data.id", equalTo("doc-1")))
            .withRequestBody(matchingJsonPath("$.data.relationships.signer.data.id", equalTo("sig-1"))));
    }

    @Test
    void updateReturnsRequirement() {
        wireMock.stubFor(patch(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements/req-1"))
            .willReturn(okJson(JsonApiFixtures.requirement("req-1", "provide_evidence", ENVELOPE_ID))));

        Requirement updated = service.update("req-1", ENVELOPE_ID,
            Requirement.UpdateParams.builder().action("provide_evidence").build());

        assertEquals("provide_evidence", updated.action());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/envelopes/" + ENVELOPE_ID + "/requirements/req-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("req-1", ENVELOPE_ID));
    }
}
