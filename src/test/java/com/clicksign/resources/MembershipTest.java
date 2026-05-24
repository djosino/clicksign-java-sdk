package com.clicksign.resources;

import com.clicksign.ClientConfig;
import com.clicksign.JsonApiFixtures;
import com.clicksign.resources.types.MembershipRole;
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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

class MembershipTest {

    private static WireMockServer wireMock;
    private Membership.Service service;

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
    void createAcceptsMembershipRoleEnum() {
        wireMock.stubFor(post(urlEqualTo("/memberships"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.membership("mem-new", "admin", "user-2"))));

        Membership created = service.create(Membership.CreateParams.builder()
            .role(MembershipRole.ADMIN)
            .userId("user-2")
            .build());

        assertEquals(MembershipRole.ADMIN, created.roleAsEnum());
        wireMock.verify(postRequestedFor(urlEqualTo("/memberships"))
            .withRequestBody(matchingJsonPath("$.data.attributes.role", equalTo("admin"))));
    }

    @Test
    void updateAcceptsMembershipRoleEnum() {
        wireMock.stubFor(put(urlEqualTo("/memberships/mem-1"))
            .willReturn(okJson(JsonApiFixtures.membership("mem-1", "member", "user-1"))));

        Membership updated = service.update("mem-1",
            Membership.UpdateParams.builder().role(MembershipRole.MEMBER).build());

        assertEquals(MembershipRole.MEMBER, updated.roleAsEnum());
        wireMock.verify(putRequestedFor(urlEqualTo("/memberships/mem-1"))
            .withRequestBody(matchingJsonPath("$.data.attributes.role", equalTo("member"))));
    }

    @Test
    void roleAsEnumReturnsNullForUnknownRole() {
        wireMock.stubFor(get(urlEqualTo("/memberships/mem-x"))
            .willReturn(okJson(JsonApiFixtures.membership("mem-x", "custom_role", "user-1"))));

        assertNull(service.retrieve("mem-x").roleAsEnum());
    }

    @Test
    void createSendsAccessibilityFields() {
        wireMock.stubFor(post(urlEqualTo("/memberships"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.membership("mem-new", "member", "user-2"))));

        service.create(Membership.CreateParams.builder()
            .role(MembershipRole.MEMBER)
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
    void filterByRoleAndUserId() {
        wireMock.stubFor(get(urlPathEqualTo("/memberships"))
            .withQueryParam("filter[role]", equalTo("admin"))
            .withQueryParam("filter[user.id]", equalTo("user-1"))
            .withQueryParam("sort", equalTo("-created"))
            .willReturn(okJson(JsonApiFixtures.membershipList(
                JsonApiFixtures.membership("mem-1", "admin", "user-1")))));

        List<Membership> memberships = service.filter()
            .role(com.clicksign.resources.types.MembershipRole.ADMIN)
            .userId("user-1")
            .order("-created")
            .fetch();

        assertEquals(1, memberships.size());
        assertEquals(MembershipRole.ADMIN, memberships.get(0).roleAsEnum());
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

    @Test
    void createRequiresRole() {
        assertThrows(IllegalArgumentException.class, () ->
            Membership.CreateParams.builder().userId("user-1").build());
    }

    @Test
    void createRequiresUserId() {
        assertThrows(IllegalArgumentException.class, () ->
            Membership.CreateParams.builder().role("admin").build());
    }

    @Test
    void createThrowsValidationException() {
        wireMock.stubFor(post(urlEqualTo("/memberships"))
            .willReturn(aResponse().withStatus(422)
                .withBody(JsonApiFixtures.errorBody("user not found"))));

        assertThrows(com.clicksign.errors.ValidationException.class, () ->
            service.create(Membership.CreateParams.builder()
                .role("admin")
                .userId("nonexistent")
                .build()));
    }
}
