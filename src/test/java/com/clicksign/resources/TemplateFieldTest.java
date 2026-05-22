package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class TemplateFieldTest {

    private static WireMockServer wireMock;
    private TemplateField.Service service;

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
        service = new TemplateField.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsTemplateFields() {
        wireMock.stubFor(get(urlEqualTo("/template_fields"))
            .willReturn(okJson(JsonApiFixtures.templateFieldList(
                JsonApiFixtures.templateField("tf-1", "nome", "tpl-1"),
                JsonApiFixtures.templateField("tf-2", "cpf", "tpl-1")
            ))));

        List<TemplateField> fields = service.list();
        assertEquals(2, fields.size());
        assertEquals("nome", fields.get(0).name());
        assertEquals("tpl-1", fields.get(0).templateId());
    }

    @Test
    void updateReturnsTemplateField() {
        wireMock.stubFor(patch(urlEqualTo("/template_fields/tf-1"))
            .willReturn(okJson(JsonApiFixtures.templateField("tf-1", "nome_completo", "tpl-1"))));

        TemplateField updated = service.update("tf-1",
            TemplateField.UpdateParams.builder().name("nome_completo").build());

        assertEquals("nome_completo", updated.name());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/template_fields/tf-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("tf-1"));
    }
}
