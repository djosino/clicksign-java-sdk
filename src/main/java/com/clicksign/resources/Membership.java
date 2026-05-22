package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.MembershipQuery;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.MembershipRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a membership linking a user to an account role. */
public final class Membership {

    private final String id;
    private final String role;
    private final boolean consumptionAccessible;
    private final boolean trackingAccessible;
    private final boolean folderManagementAccessible;
    private final String userId;
    private final String createdAt;
    private final String modifiedAt;

    /**
     * Constructs from a JSON:API resource object.
     *
     * @param obj resource object
     * @return new instance
     */
    public static Membership from(JsonApiParser.ResourceObject obj) {
        return new Membership(obj);
    }

    private Membership(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id                          = obj.id();
        this.role                        = str(a.get("role"));
        this.consumptionAccessible       = bool(a.get("consumption_accessible"));
        this.trackingAccessible          = bool(a.get("tracking_accessible"));
        this.folderManagementAccessible  = bool(a.get("folder_management_accessible"));
        this.userId                      = obj.relationshipId("user");
        this.createdAt                   = str(a.get("created"));
        this.modifiedAt                  = str(a.get("modified"));
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
     * Returns the role.
     *
     * @return role
     */
    public String role() {
        return role;
    }

    /**
     * Returns the role as an enum.
     *
     * @return role enum
     */
    public MembershipRole roleAsEnum() {
        return ApiStringEnum.tryParse(MembershipRole.class, role);
    }

    /**
     * Returns whether consumption features are accessible.
     *
     * @return consumption accessible flag
     */
    public boolean consumptionAccessible() {
        return consumptionAccessible;
    }

    /**
     * Returns whether tracking features are accessible.
     *
     * @return tracking accessible flag
     */
    public boolean trackingAccessible() {
        return trackingAccessible;
    }

    /**
     * Returns whether folder management is accessible.
     *
     * @return folder management accessible flag
     */
    public boolean folderManagementAccessible() {
        return folderManagementAccessible;
    }

    /**
     * Returns the user id.
     *
     * @return user id
     */
    public String userId() {
        return userId;
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
        return "Membership{id='" + id + "', role='" + role + "', userId='" + userId + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static boolean bool(Object o) {
        return Boolean.TRUE.equals(o) || "true".equals(str(o));
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Membership operations. */
    public static final class Service {

        private static final String ENDPOINT = "/memberships";
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
        public List<Membership> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Membership> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Membership(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Returns a fluent query builder.
         *
         * @return query builder
         */
        public MembershipQuery filter() {
            return new MembershipQuery(http);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id resource id
         * @return resource
         */
        public Membership retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Membership(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public Membership create(CreateParams params) {
            String body = JsonApiSerializer.dump("memberships", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post(ENDPOINT, body);
            return new Membership(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Updates resource.
         *
         * @param id     resource id
         * @param params update parameters
         * @return updated resource
         */
        public Membership update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("memberships", id, params.toAttributes(), null);
            String raw  = http.put(ENDPOINT + "/" + id, body);
            return new Membership(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Deletes resource by id.
         *
         * @param id resource id
         */
        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating a membership. */
    public static final class CreateParams {

        private final String role;
        private final String userId;
        private final Boolean consumptionAccessible;
        private final Boolean trackingAccessible;
        private final Boolean folderManagementAccessible;

        private CreateParams(Builder b) {
            this.role                       = b.role;
            this.userId                     = b.userId;
            this.consumptionAccessible      = b.consumptionAccessible;
            this.trackingAccessible         = b.trackingAccessible;
            this.folderManagementAccessible = b.folderManagementAccessible;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", role);
            if (consumptionAccessible      != null) {
                m.put("consumption_accessible",       consumptionAccessible);
            }
            if (trackingAccessible         != null) {
                m.put("tracking_accessible",          trackingAccessible);
            }
            if (folderManagementAccessible != null) {
                m.put("folder_management_accessible", folderManagementAccessible);
            }
            return m;
        }

        Map<String, Object> toRelationships() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "users");
            data.put("id",   userId);
            Map<String, Object> rel = new LinkedHashMap<>();
            rel.put("data", data);
            Map<String, Object> rels = new LinkedHashMap<>();
            rels.put("user", rel);
            return rels;
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
            private String role;
            private String userId;
            private Boolean consumptionAccessible;
            private Boolean trackingAccessible;
            private Boolean folderManagementAccessible;

            private Builder() {}

            /**
             * Sets role.
             *
             * @param v value
             * @return this builder
             */
            public Builder role(String v) {
                this.role = v;
                return this;
            }

            /**
             * Sets role.
             *
             * @param v value
             * @return this builder
             */
            public Builder role(MembershipRole v) {
                return role(v != null ? v.apiValue() : null);
            }

            /**
             * Sets user id.
             *
             * @param v value
             * @return this builder
             */
            public Builder userId(String v) {
                this.userId = v;
                return this;
            }

            /**
             * Sets consumption accessible flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder consumptionAccessible(boolean v) {
                this.consumptionAccessible = v;
                return this;
            }

            /**
             * Sets tracking accessible flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder trackingAccessible(boolean v) {
                this.trackingAccessible = v;
                return this;
            }

            /**
             * Sets folder management accessible flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder folderManagementAccessible(boolean v) {
                this.folderManagementAccessible = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (role == null || role.isBlank()) {
                    throw new IllegalArgumentException("role is required");
                }
                if (userId == null || userId.isBlank()) {
                    throw new IllegalArgumentException("userId is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating a membership. */
    public static final class UpdateParams {

        private final String role;
        private final Boolean consumptionAccessible;
        private final Boolean trackingAccessible;
        private final Boolean folderManagementAccessible;

        private UpdateParams(Builder b) {
            this.role                       = b.role;
            this.consumptionAccessible      = b.consumptionAccessible;
            this.trackingAccessible         = b.trackingAccessible;
            this.folderManagementAccessible = b.folderManagementAccessible;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (role                       != null) {
                m.put("role", role);
            }
            if (consumptionAccessible      != null) {
                m.put("consumption_accessible",       consumptionAccessible);
            }
            if (trackingAccessible         != null) {
                m.put("tracking_accessible",          trackingAccessible);
            }
            if (folderManagementAccessible != null) {
                m.put("folder_management_accessible", folderManagementAccessible);
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
            private String role;
            private Boolean consumptionAccessible;
            private Boolean trackingAccessible;
            private Boolean folderManagementAccessible;

            private Builder() {}

            /**
             * Sets role.
             *
             * @param v value
             * @return this builder
             */
            public Builder role(String v) {
                this.role = v;
                return this;
            }

            /**
             * Sets role.
             *
             * @param v value
             * @return this builder
             */
            public Builder role(MembershipRole v) {
                return role(v != null ? v.apiValue() : null);
            }

            /**
             * Sets consumption accessible flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder consumptionAccessible(boolean v) {
                this.consumptionAccessible = v;
                return this;
            }

            /**
             * Sets tracking accessible flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder trackingAccessible(boolean v) {
                this.trackingAccessible = v;
                return this;
            }

            /**
             * Sets folder management accessible flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder folderManagementAccessible(boolean v) {
                this.folderManagementAccessible = v;
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
