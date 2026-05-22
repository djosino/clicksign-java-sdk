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

class AccessControlListTest {

    private static WireMockServer wireMock;
    private AccessControlList.Service service;

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
        service = new AccessControlList.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void createReturnsAccessControlList() {
        wireMock.stubFor(post(urlEqualTo("/access_control_lists"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.accessControlList("acl-1", "fld-1", "grp-1"))));

        AccessControlList acl = service.create("fld-1", "grp-1");

        assertEquals("acl-1", acl.id());
        assertEquals("fld-1", acl.folderId());
        assertEquals("grp-1", acl.groupId());

        wireMock.verify(postRequestedFor(urlEqualTo("/access_control_lists"))
            .withRequestBody(matchingJsonPath("$.data.relationships.folder.data.id", equalTo("fld-1")))
            .withRequestBody(matchingJsonPath("$.data.relationships.group.data.id", equalTo("grp-1"))));
    }

    @Test
    void destroySendsRelationships() {
        wireMock.stubFor(delete(urlEqualTo("/access_control_lists"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.destroy("fld-1", "grp-1"));

        wireMock.verify(deleteRequestedFor(urlEqualTo("/access_control_lists"))
            .withRequestBody(matchingJsonPath("$.data.relationships.folder.data.id", equalTo("fld-1")))
            .withRequestBody(matchingJsonPath("$.data.relationships.group.data.id", equalTo("grp-1"))));
    }
}
