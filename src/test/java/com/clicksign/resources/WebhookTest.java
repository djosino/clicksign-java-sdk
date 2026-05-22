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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class WebhookTest {

    private static WireMockServer wireMock;
    private Webhook.Service service;

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
        service = new Webhook.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsWebhooks() {
        wireMock.stubFor(get(urlEqualTo("/webhooks"))
            .willReturn(okJson(JsonApiFixtures.webhookList(
                JsonApiFixtures.webhook("wh-1", "https://a.example/hook", "active"),
                JsonApiFixtures.webhook("wh-2", "https://b.example/hook", "inactive")
            ))));

        List<Webhook> webhooks = service.list();
        assertEquals(2, webhooks.size());
        assertEquals("wh-1", webhooks.get(0).id());
        assertEquals("https://a.example/hook", webhooks.get(0).endpoint());
    }

    @Test
    void retrieveReturnsWebhook() {
        wireMock.stubFor(get(urlEqualTo("/webhooks/wh-1"))
            .willReturn(okJson(JsonApiFixtures.webhook("wh-1", "https://hook.example", "active"))));

        Webhook webhook = service.retrieve("wh-1");
        assertEquals("wh-1", webhook.id());
        assertEquals("sec123", webhook.secret());
    }

    @Test
    void createWithSecretSendsCorrectBody() {
        wireMock.stubFor(post(urlEqualTo("/webhooks"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.webhook("wh-new", "https://new.example", "active"))));

        service.create(Webhook.CreateParams.builder()
            .endpoint("https://new.example")
            .addEvent("sign")
            .secret("my-secret")
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/webhooks"))
            .withRequestBody(matchingJsonPath("$.data.attributes.secret", equalTo("my-secret")))
            .withRequestBody(matchingJsonPath("$.data.attributes.endpoint", equalTo("https://new.example"))));
    }

    @Test
    void updateReturnsWebhook() {
        wireMock.stubFor(patch(urlEqualTo("/webhooks/wh-1"))
            .willReturn(okJson(JsonApiFixtures.webhook("wh-1", "https://updated.example", "active"))));

        Webhook updated = service.update("wh-1",
            Webhook.UpdateParams.builder().endpoint("https://updated.example").build());

        assertEquals("https://updated.example", updated.endpoint());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/webhooks/wh-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("wh-1"));
    }
}
