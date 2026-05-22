package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class AutoSignatureTermTest {

    private static WireMockServer wireMock;
    private AutoSignatureTerm.Service service;

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
        service = new AutoSignatureTerm.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void createReturnsTerm() {
        wireMock.stubFor(post(urlEqualTo("/auto_signature/terms"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.autoSignatureTerm("term-1", "João Silva", "joao@example.com"))));

        AutoSignatureTerm term = service.create(AutoSignatureTerm.CreateParams.builder()
            .signerName("João Silva")
            .signerEmail("joao@example.com")
            .signerDocumentation("123.456.789-10")
            .signerBirthday("1990-01-01")
            .apiEmail("api@example.com")
            .adminEmail("admin@example.com")
            .build());

        assertEquals("term-1", term.id());
        assertEquals("joao@example.com", term.email());

        wireMock.verify(postRequestedFor(urlEqualTo("/auto_signature/terms"))
            .withRequestBody(matchingJsonPath("$.data.attributes.signer.name", equalTo("João Silva")))
            .withRequestBody(matchingJsonPath("$.data.attributes.api_email", equalTo("api@example.com"))));
    }

    @Test
    void retrieveReturnsTerm() {
        wireMock.stubFor(get(urlEqualTo("/auto_signature/terms/term-1"))
            .willReturn(aResponse().withStatus(200)
                .withBody(JsonApiFixtures.autoSignatureTerm("term-1", "João Silva", "joao@example.com"))));

        AutoSignatureTerm term = service.retrieve("term-1");

        assertEquals("term-1", term.id());
        assertEquals("João Silva", term.name());
        wireMock.verify(getRequestedFor(urlEqualTo("/auto_signature/terms/term-1")));
    }
}
