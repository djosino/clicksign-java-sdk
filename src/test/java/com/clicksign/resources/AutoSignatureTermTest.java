package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoSignatureTermTest {

    private static WireMockServer wireMock;
    private AutoSignatureTerm.Service service;

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
}
