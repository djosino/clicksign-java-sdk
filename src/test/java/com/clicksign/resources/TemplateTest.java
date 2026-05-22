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

class TemplateTest {

    private static WireMockServer wireMock;
    private Template.Service service;

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
        service = new Template.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void createReturnsTemplate() {
        wireMock.stubFor(post(urlEqualTo("/templates"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.template("tpl-1", "Contrato Padrão"))));

        Template template = service.create(Template.CreateParams.builder()
            .name("Contrato Padrão")
            .contentBase64("data:application/vnd.openxmlformats-officedocument.wordprocessingml.document;base64,abc")
            .color("#FF0000")
            .build());

        assertEquals("tpl-1", template.id());
        assertEquals("Contrato Padrão", template.name());
    }

    @Test
    void createSendsCorrectBody() {
        wireMock.stubFor(post(urlEqualTo("/templates"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.template("tpl-1", "Modelo"))));

        service.create(Template.CreateParams.builder()
            .name("Modelo")
            .contentBase64("base64content")
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/templates"))
            .withRequestBody(matchingJsonPath("$.data.type", equalTo("templates")))
            .withRequestBody(matchingJsonPath("$.data.attributes.name", equalTo("Modelo")))
            .withRequestBody(matchingJsonPath("$.data.attributes.content_base64", equalTo("base64content"))));
    }

    @Test
    void updateReturnsTemplate() {
        wireMock.stubFor(patch(urlEqualTo("/templates/tpl-1"))
            .willReturn(okJson(JsonApiFixtures.template("tpl-1", "Nome Atualizado"))));

        Template updated = service.update("tpl-1",
            Template.UpdateParams.builder().name("Nome Atualizado").build());

        assertEquals("Nome Atualizado", updated.name());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/templates/tpl-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("tpl-1"));
    }

    @Test
    void createRequiresContentBase64() {
        assertThrows(IllegalArgumentException.class, () ->
            Template.CreateParams.builder().name("Modelo").build());
    }
}
