package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.Instrumentation;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    private static WireMockServer wireMock;
    private Group.Service service;

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
        service = new Group.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsGroups() {
        wireMock.stubFor(get(urlEqualTo("/groups"))
            .willReturn(okJson(JsonApiFixtures.groupList(
                JsonApiFixtures.group("grp-1", "Financeiro"),
                JsonApiFixtures.group("grp-2", "Juridico")
            ))));

        List<Group> groups = service.list();
        assertEquals(2, groups.size());
        assertEquals("Financeiro", groups.get(0).name());
    }

    @Test
    void retrieveReturnsGroup() {
        wireMock.stubFor(get(urlEqualTo("/groups/grp-1"))
            .willReturn(okJson(JsonApiFixtures.group("grp-1", "Financeiro"))));

        assertEquals("grp-1", service.retrieve("grp-1").id());
    }

    @Test
    void createReturnsGroup() {
        wireMock.stubFor(post(urlEqualTo("/groups"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.group("grp-new", "Novo Grupo"))));

        Group group = service.create(Group.CreateParams.builder().name("Novo Grupo").build());
        assertEquals("grp-new", group.id());
    }

    @Test
    void updateReturnsGroup() {
        wireMock.stubFor(patch(urlEqualTo("/groups/grp-1"))
            .willReturn(okJson(JsonApiFixtures.group("grp-1", "Financeiro Atualizado"))));

        assertEquals("Financeiro Atualizado", service.update("grp-1",
            Group.UpdateParams.builder().name("Financeiro Atualizado").build()).name());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/groups/grp-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("grp-1"));
    }

    @Test
    void addUsersPostsRelationship() {
        wireMock.stubFor(post(urlEqualTo("/groups/grp-1/relationships/users"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.addUsers("grp-1", List.of("user-1", "user-2")));

        wireMock.verify(postRequestedFor(urlEqualTo("/groups/grp-1/relationships/users"))
            .withRequestBody(matchingJsonPath("$.data[0].id", equalTo("user-1")))
            .withRequestBody(matchingJsonPath("$.data[1].id", equalTo("user-2"))));
    }

    @Test
    void removeUsersDeletesRelationship() {
        wireMock.stubFor(delete(urlEqualTo("/groups/grp-1/relationships/users"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.removeUsers("grp-1", List.of("user-1")));

        wireMock.verify(deleteRequestedFor(urlEqualTo("/groups/grp-1/relationships/users"))
            .withRequestBody(matchingJsonPath("$.data[0].type", equalTo("users"))));
    }
}
