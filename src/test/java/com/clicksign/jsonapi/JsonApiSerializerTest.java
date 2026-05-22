package com.clicksign.jsonapi;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonApiSerializerTest {

    @Test
    void dumpsCreateBody() {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("name", "Contrato");
        attrs.put("locale", "pt-BR");
        String json = JsonApiSerializer.dump("envelopes", attrs);
        assertTrue(json.contains("\"type\":\"envelopes\""));
        assertTrue(json.contains("\"name\":\"Contrato\""));
        assertFalse(json.contains("\"id\""));
        assertFalse(json.contains("\"relationships\""));
    }

    @Test
    void dumpsUpdateBodyWithId() {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("status", "running");
        String json = JsonApiSerializer.dump("envelopes", "env-1", attrs, null);
        assertTrue(json.contains("\"id\":\"env-1\""));
        assertTrue(json.contains("\"status\":\"running\""));
    }

    @Test
    void dumpsRelationships() {
        Map<String, Object> folderData = new LinkedHashMap<>();
        folderData.put("type", "folders");
        folderData.put("id", "folder-1");
        Map<String, Object> folderRel = new LinkedHashMap<>();
        folderRel.put("data", folderData);
        Map<String, Object> rels = new LinkedHashMap<>();
        rels.put("folder", folderRel);

        String json = JsonApiSerializer.dump("envelopes", null, Collections.emptyMap(), rels);
        assertTrue(json.contains("\"relationships\""));
        assertTrue(json.contains("\"folder-1\""));
    }

    @Test
    void omitsRelationshipsWhenEmpty() {
        String json = JsonApiSerializer.dump("envelopes", null, Collections.emptyMap(), Collections.emptyMap());
        assertFalse(json.contains("\"relationships\""));
    }

    @Test
    void serializesBooleans() {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("auto_close", true);
        attrs.put("refusable", false);
        String json = JsonApiSerializer.dump("envelopes", attrs);
        assertTrue(json.contains("\"auto_close\":true"));
        assertTrue(json.contains("\"refusable\":false"));
    }

    @Test
    void serializesNull() {
        assertEquals("null", JsonApiSerializer.toJson(null));
    }

    @Test
    void serializesList() {
        List<String> list = Arrays.asList("sign", "close");
        String json = JsonApiSerializer.toJson(list);
        assertEquals("[\"sign\",\"close\"]", json);
    }
}
