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

class MembershipTest {

    private static WireMockServer wireMock;
    private Membership.Service service;

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
        service = new Membership.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void updateUsesPut() {
        wireMock.stubFor(put(urlEqualTo("/memberships/mem-1"))
            .willReturn(okJson(JsonApiFixtures.membership("mem-1", "admin", "user-1"))));

        Membership updated = service.update("mem-1",
            Membership.UpdateParams.builder()
                .role("admin")
                .consumptionAccessible(true)
                .trackingAccessible(true)
                .folderManagementAccessible(false)
                .build());

        assertEquals("admin", updated.role());
        assertEquals("mem-1", updated.id());

        wireMock.verify(putRequestedFor(urlEqualTo("/memberships/mem-1"))
            .withRequestBody(matchingJsonPath("$.data.attributes.role", equalTo("admin")))
            .withRequestBody(matchingJsonPath("$.data.attributes.consumption_accessible", equalTo("true")))
            .withRequestBody(matchingJsonPath("$.data.attributes.tracking_accessible", equalTo("true")))
            .withRequestBody(matchingJsonPath("$.data.attributes.folder_management_accessible", equalTo("false"))));
    }

    @Test
    void createSendsAccessibilityFields() {
        wireMock.stubFor(post(urlEqualTo("/memberships"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.membership("mem-new", "member", "user-2"))));

        service.create(Membership.CreateParams.builder()
            .role("member")
            .userId("user-2")
            .consumptionAccessible(true)
            .trackingAccessible(false)
            .folderManagementAccessible(true)
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/memberships"))
            .withRequestBody(matchingJsonPath("$.data.attributes.consumption_accessible", equalTo("true")))
            .withRequestBody(matchingJsonPath("$.data.attributes.tracking_accessible", equalTo("false")))
            .withRequestBody(matchingJsonPath("$.data.attributes.folder_management_accessible", equalTo("true"))));
    }

    @Test
    void listReturnsMemberships() {
        wireMock.stubFor(get(urlEqualTo("/memberships"))
            .willReturn(okJson(JsonApiFixtures.membershipList(
                JsonApiFixtures.membership("mem-1", "admin", "user-1"),
                JsonApiFixtures.membership("mem-2", "member", "user-2")
            ))));

        List<Membership> memberships = service.list();
        assertEquals(2, memberships.size());
        assertEquals("admin", memberships.get(0).role());
        assertEquals("user-1", memberships.get(0).userId());
    }

    @Test
    void retrieveReturnsMembership() {
        wireMock.stubFor(get(urlEqualTo("/memberships/mem-1"))
            .willReturn(okJson(JsonApiFixtures.membership("mem-1", "admin", "user-1"))));

        Membership membership = service.retrieve("mem-1");
        assertEquals("mem-1", membership.id());
        assertTrue(membership.consumptionAccessible());
    }

    @Test
    void deleteDoesNotThrow() {
        wireMock.stubFor(delete(urlEqualTo("/memberships/mem-1"))
            .willReturn(aResponse().withStatus(204).withBody("")));

        assertDoesNotThrow(() -> service.delete("mem-1"));
    }
}
