package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class UserTest {

    private static WireMockServer wireMock;
    private User.Service service;

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
        service = new User.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsUsers() {
        wireMock.stubFor(get(urlEqualTo("/users"))
            .willReturn(okJson(JsonApiFixtures.userList(
                JsonApiFixtures.user("user-1", "Alice", "alice@example.com"),
                JsonApiFixtures.user("user-2", "Bob", "bob@example.com")
            ))));

        List<User> users = service.list();
        assertEquals(2, users.size());
        assertEquals("Alice", users.get(0).name());
    }

    @Test
    void retrieveReturnsUser() {
        wireMock.stubFor(get(urlEqualTo("/users/user-1"))
            .willReturn(okJson(JsonApiFixtures.user("user-1", "Alice", "alice@example.com"))));

        User user = service.retrieve("user-1");
        assertEquals("user-1", user.id());
        assertEquals("alice@example.com", user.email());
    }

    @Test
    void meReturnsCurrentUser() {
        wireMock.stubFor(get(urlEqualTo("/users/me"))
            .willReturn(okJson(JsonApiFixtures.user("user-me", "Me", "me@example.com"))));

        User user = service.me();
        assertEquals("user-me", user.id());
        assertEquals("me@example.com", user.email());
    }

    @Test
    void createReturnsUser() {
        wireMock.stubFor(post(urlEqualTo("/users"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.user("user-new", "Carol", "carol@example.com"))));

        User user = service.create(User.CreateParams.builder()
            .name("Carol")
            .email("carol@example.com")
            .phoneNumber("11999998888")
            .build());

        assertEquals("user-new", user.id());
        assertEquals("Carol", user.name());
    }
}
