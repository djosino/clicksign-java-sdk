package com.clicksign.resources.notarial;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.errors.ValidationException;
import com.clicksign.jsonapi.BulkOperationsClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class BulkRequirementTest {

    private static WireMockServer wireMock;
    private BulkRequirement.Service service;
    private static final String ENVELOPE_ID = "aaaa-aaaa";
    private static final String SIGNER_ID   = "sign-1111";
    private static final String DOCUMENT_ID = "doc-2222";
    private static final String REQ_ID      = "req-3333";
    private String bulkUrl;

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
        String baseUrl = "http://localhost:" + wireMock.port();
        bulkUrl = "/envelopes/" + ENVELOPE_ID + "/bulk_requirements";
        ClientConfig config = ClientConfig.builder().apiKey("test-token").baseUrl(baseUrl).build();
        service = new BulkRequirement.Service(new BulkOperationsClient(config));
    }

    @Test
    void createAddAgreeReturnsSuccessResponse() {
        wireMock.stubFor(post(urlEqualTo(bulkUrl))
            .willReturn(okJson(JsonApiFixtures.atomicResults(REQ_ID, "agree"))));

        BulkRequirement.Response response = service.create(ENVELOPE_ID,
            ops -> ops.addAgree(SIGNER_ID, DOCUMENT_ID, "sign"));

        assertTrue(response.isSuccess());
        assertEquals(1, response.requirements().size());
        assertEquals(REQ_ID, response.requirements().get(0).id());
        assertEquals("agree", response.requirements().get(0).action());
        assertEquals(ENVELOPE_ID, response.requirements().get(0).envelopeId());
    }

    @Test
    void createSendsCorrectAtomicOperationsPayload() {
        wireMock.stubFor(post(urlEqualTo(bulkUrl))
            .willReturn(okJson(JsonApiFixtures.atomicResults(REQ_ID, "agree"))));

        service.create(ENVELOPE_ID, ops -> ops.addAgree(SIGNER_ID, DOCUMENT_ID, "sign"));

        wireMock.verify(postRequestedFor(urlEqualTo(bulkUrl))
            .withRequestBody(matchingJsonPath("$['atomic:operations'][0].op", equalTo("add")))
            .withRequestBody(matchingJsonPath("$['atomic:operations'][0].data.type", equalTo("requirements")))
            .withRequestBody(matchingJsonPath("$['atomic:operations'][0].data.attributes.action", equalTo("agree")))
            .withRequestBody(matchingJsonPath("$['atomic:operations'][0].data.relationships.signer.data.id", equalTo(SIGNER_ID))));
    }

    @Test
    void createWithSlotErrorReturnsFailedResult() {
        wireMock.stubFor(post(urlEqualTo(bulkUrl))
            .willReturn(aResponse().withStatus(404)
                .withBody(JsonApiFixtures.atomicResultsWithError("not found"))));

        BulkRequirement.Response response = service.create(ENVELOPE_ID,
            ops -> ops.remove("req-old"));

        assertFalse(response.isSuccess());
        assertEquals(1, response.failures().size());
        assertFalse(response.failures().get(0).errors().isEmpty());
    }

    @Test
    void createThrowsValidationExceptionForTopLevelErrors() {
        wireMock.stubFor(post(urlEqualTo(bulkUrl))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"errors\":[{\"detail\":\"envelope invalid\"}]}")));

        assertThrows(ValidationException.class,
            () -> service.create(ENVELOPE_ID, ops -> ops.addAgree(SIGNER_ID, DOCUMENT_ID, "sign")));
    }

    @Test
    void addAgreeRequiresRole() {
        assertThrows(IllegalArgumentException.class,
            () -> new BulkRequirement.Operations().addAgree(SIGNER_ID, DOCUMENT_ID, ""));
    }

    @Test
    void addProvideEvidenceRequiresAuth() {
        assertThrows(IllegalArgumentException.class,
            () -> new BulkRequirement.Operations().addProvideEvidence(SIGNER_ID, DOCUMENT_ID, ""));
    }

    @Test
    void addRubricateRequiresPagesOrRubricField() {
        assertThrows(IllegalArgumentException.class,
            () -> new BulkRequirement.Operations().addRubricate(SIGNER_ID, DOCUMENT_ID, null, null, null));
    }

    @Test
    void addRubricateRejectsInvalidKind() {
        assertThrows(IllegalArgumentException.class,
            () -> new BulkRequirement.Operations().addRubricate(SIGNER_ID, DOCUMENT_ID, "all", null, "invalid"));
    }
}
