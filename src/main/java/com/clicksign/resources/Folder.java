package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a Clicksign folder. */
public final class Folder {

    private final String id;
    private final String name;
    private final String path;
    private final boolean inRoot;
    private final String folderId;
    private final List<String> childFolderIds;
    private final String createdAt;
    private final String modifiedAt;

    @SuppressWarnings("unchecked")
    private Folder(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id         = obj.id();
        this.name       = str(a.get("name"));
        this.path       = str(a.get("path"));
        this.inRoot     = bool(a.get("in_root"));
        this.folderId   = obj.relationshipId("folder");
        this.createdAt  = str(a.get("created"));
        this.modifiedAt = str(a.get("modified"));

        Map<String, Object> rels = obj.relationships();
        List<String> childIds = new ArrayList<>();
        Object foldersRel = rels.get("folders");
        if (foldersRel instanceof Map) {
            Object data = ((Map<String, Object>) foldersRel).get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (item instanceof Map) {
                        Object fid = ((Map<String, Object>) item).get("id");
                        if (fid != null) {
                            childIds.add(fid.toString());
                        }
                    }
                }
            }
        }
        this.childFolderIds = Collections.unmodifiableList(childIds);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String path() {
        return path;
    }

    public boolean inRoot() {
        return inRoot;
    }

    public String folderId() {
        return folderId;
    }

    public List<String> childFolderIds() {
        return childFolderIds;
    }

    public String createdAt() {
        return createdAt;
    }

    public String modifiedAt() {
        return modifiedAt;
    }

    @Override
    public String toString() {
        return "Folder{id='" + id + "', name='" + name + "', path='" + path + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static boolean bool(Object o) {
        return Boolean.TRUE.equals(o) || "true".equals(str(o));
    }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/folders";
        private final HttpClient http;

        public Service(HttpClient http) {
            this.http = http;
        }

        public List<Folder> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Folder> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Folder(obj));
            }
            return Collections.unmodifiableList(result);
        }

        public Folder retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Folder(JsonApiParser.parse(raw).firstData());
        }

        public Folder create(CreateParams params) {
            String body = JsonApiSerializer.dump("folders", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post(ENDPOINT, body);
            return new Folder(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        private final String name;
        private final String folderId;

        private CreateParams(Builder b) {
            this.name     = b.name;
            this.folderId = b.folderId;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            return m;
        }

        Map<String, Object> toRelationships() {
            if (folderId == null) {
                return Collections.emptyMap();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "folders");
            data.put("id",   folderId);
            Map<String, Object> rel = new LinkedHashMap<>();
            rel.put("data", data);
            Map<String, Object> rels = new LinkedHashMap<>();
            rels.put("folder", rel);
            return rels;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String name;
            private String folderId;

            private Builder() {}

            public Builder name(String v) {
                this.name = v;
                return this;
            }

            public Builder folderId(String v) {
                this.folderId = v;
                return this;
            }

            public CreateParams build() {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("name is required");
                }
                return new CreateParams(this);
            }
        }
    }
}
