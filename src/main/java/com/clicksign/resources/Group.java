package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.*;

/** Represents a Clicksign user group. */
public final class Group {

    private final String id;
    private final String name;
    private final String createdAt;
    private final String modifiedAt;

    private Group(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id         = obj.id();
        this.name       = str(a.get("name"));
        this.createdAt  = str(a.get("created"));
        this.modifiedAt = str(a.get("modified"));
    }

    public String id()          { return id; }
    public String name()        { return name; }
    public String createdAt()   { return createdAt; }
    public String modifiedAt()  { return modifiedAt; }

    @Override
    public String toString() {
        return "Group{id='" + id + "', name='" + name + "'}";
    }

    private static String str(Object o) { return o != null ? o.toString() : null; }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/groups";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<Group> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Group> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) result.add(new Group(obj));
            return Collections.unmodifiableList(result);
        }

        public Group retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Group(JsonApiParser.parse(raw).firstData());
        }

        public Group create(CreateParams params) {
            String body = JsonApiSerializer.dump("groups", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new Group(JsonApiParser.parse(raw).firstData());
        }

        public Group update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("groups", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Group(JsonApiParser.parse(raw).firstData());
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }

        public void addUsers(String groupId, List<String> userIds) {
            List<Map<String, Object>> data = new ArrayList<>();
            for (String uid : userIds) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", "users");
                entry.put("id",   uid);
                data.add(entry);
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("data", data);
            http.post("/groups/" + groupId + "/relationships/users",
                    JsonApiSerializer.toJson(body));
        }

        public void removeUsers(String groupId, List<String> userIds) {
            List<Map<String, Object>> data = new ArrayList<>();
            for (String uid : userIds) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", "users");
                entry.put("id",   uid);
                data.add(entry);
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("data", data);
            http.delete("/groups/" + groupId + "/relationships/users",
                    JsonApiSerializer.toJson(body));
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        private final String name;

        private CreateParams(Builder b) { this.name = b.name; }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;

            private Builder() {}

            public Builder name(String v) { this.name = v; return this; }

            public CreateParams build() {
                if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    public static final class UpdateParams {

        private final String name;

        private UpdateParams(Builder b) { this.name = b.name; }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name != null) m.put("name", name);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;

            private Builder() {}

            public Builder name(String v) { this.name = v; return this; }

            public UpdateParams build() { return new UpdateParams(this); }
        }
    }
}
