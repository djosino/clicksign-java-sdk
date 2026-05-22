package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Returns the id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Returns the name.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the created at timestamp.
     *
     * @return created at
     */
    public String createdAt() {
        return createdAt;
    }

    /**
     * Returns the modified at timestamp.
     *
     * @return modified at
     */
    public String modifiedAt() {
        return modifiedAt;
    }

    @Override
    public String toString() {
        return "Group{id='" + id + "', name='" + name + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Group operations. */
    public static final class Service {

        private static final String ENDPOINT = "/groups";
        private final HttpClient http;

        /**
         * Constructs service.
         *
         * @param http HTTP client
         */
        public Service(HttpClient http) {
            this.http = http;
        }

        /**
         * Lists all resources.
         *
         * @return unmodifiable list
         */
        public List<Group> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Group> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Group(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id resource id
         * @return resource
         */
        public Group retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Group(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public Group create(CreateParams params) {
            String body = JsonApiSerializer.dump("groups", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new Group(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Updates resource.
         *
         * @param id     resource id
         * @param params update parameters
         * @return updated resource
         */
        public Group update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("groups", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Group(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Deletes resource by id.
         *
         * @param id resource id
         */
        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }

        /**
         * Adds users to the group.
         *
         * @param groupId group id
         * @param userIds user ids to add
         */
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

        /**
         * Removes users from the group.
         *
         * @param groupId group id
         * @param userIds user ids to remove
         */
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

    /** Parameters for creating a group. */
    public static final class CreateParams {

        private final String name;

        private CreateParams(Builder b) {
            this.name = b.name;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            return m;
        }

        /**
         * Returns a new builder.
         *
         * @return new builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link CreateParams}. */
        public static final class Builder {
            private String name;

            private Builder() {}

            /**
             * Sets name.
             *
             * @param v value
             * @return this builder
             */
            public Builder name(String v) {
                this.name = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("name is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating a group. */
    public static final class UpdateParams {

        private final String name;

        private UpdateParams(Builder b) {
            this.name = b.name;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name != null) {
                m.put("name", name);
            }
            return m;
        }

        /**
         * Returns a new builder.
         *
         * @return new builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link UpdateParams}. */
        public static final class Builder {
            private String name;

            private Builder() {}

            /**
             * Sets name.
             *
             * @param v value
             * @return this builder
             */
            public Builder name(String v) {
                this.name = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             */
            public UpdateParams build() {
                return new UpdateParams(this);
            }
        }
    }
}
