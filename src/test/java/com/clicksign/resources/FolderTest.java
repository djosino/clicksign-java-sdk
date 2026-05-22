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

class FolderTest {

    private static WireMockServer wireMock;
    private Folder.Service service;

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
        service = new Folder.Service(new HttpClient(config, new Instrumentation()));
    }

    @Test
    void listReturnsFolders() {
        wireMock.stubFor(get(urlEqualTo("/folders"))
            .willReturn(okJson(JsonApiFixtures.folderList(
                JsonApiFixtures.folder("fld-1", "Contratos"),
                JsonApiFixtures.folder("fld-2", "RH")
            ))));

        List<Folder> folders = service.list();
        assertEquals(2, folders.size());
        assertEquals("Contratos", folders.get(0).name());
    }

    @Test
    void retrieveReturnsFolder() {
        wireMock.stubFor(get(urlEqualTo("/folders/fld-1"))
            .willReturn(okJson(JsonApiFixtures.folder("fld-1", "Contratos"))));

        Folder folder = service.retrieve("fld-1");
        assertEquals("fld-1", folder.id());
        assertTrue(folder.inRoot());
    }

    @Test
    void createWithFolderIdSendsRelationship() {
        wireMock.stubFor(post(urlEqualTo("/folders"))
            .willReturn(aResponse().withStatus(201)
                .withBody(JsonApiFixtures.folderWithParent("fld-child", "Sub", "fld-parent"))));

        service.create(Folder.CreateParams.builder()
            .name("Sub")
            .folderId("fld-parent")
            .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/folders"))
            .withRequestBody(matchingJsonPath("$.data.relationships.folder.data.id", equalTo("fld-parent"))));
    }
}
