package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.*;

/** Represents folder-group access control. Supports create and destroy only. */
public final class AccessControlList {

    private final String id;
    private final String folderId;
    private final String groupId;

    private AccessControlList(JsonApiParser.ResourceObject obj) {
        this.id       = obj.id();
        this.folderId = obj.relationshipId("folder");
        this.groupId  = obj.relationshipId("group");
    }

    public String id()       { return id; }
    public String folderId() { return folderId; }
    public String groupId()  { return groupId; }

    @Override
    public String toString() {
        return "AccessControlList{id='" + id + "', folderId='" + folderId + "', groupId='" + groupId + "'}";
    }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/access_control_lists";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public AccessControlList create(String folderId, String groupId) {
            Map<String, Object> rels = buildRelationships(folderId, groupId);
            String body = JsonApiSerializer.dump("access_control_lists", null, Collections.emptyMap(), rels);
            String raw  = http.post(ENDPOINT, body);
            return new AccessControlList(JsonApiParser.parse(raw).firstData());
        }

        public void destroy(String folderId, String groupId) {
            Map<String, Object> rels = buildRelationships(folderId, groupId);
            String body = JsonApiSerializer.dump("access_control_lists", null, Collections.emptyMap(), rels);
            http.delete(ENDPOINT, body);
        }

        private static Map<String, Object> buildRelationships(String folderId, String groupId) {
            Map<String, Object> folderData = new LinkedHashMap<>();
            folderData.put("type", "folders");
            folderData.put("id",   folderId);
            Map<String, Object> folderRel = new LinkedHashMap<>();
            folderRel.put("data", folderData);

            Map<String, Object> groupData = new LinkedHashMap<>();
            groupData.put("type", "groups");
            groupData.put("id",   groupId);
            Map<String, Object> groupRel = new LinkedHashMap<>();
            groupRel.put("data", groupData);

            Map<String, Object> rels = new LinkedHashMap<>();
            rels.put("folder", folderRel);
            rels.put("group",  groupRel);
            return rels;
        }
    }
}
