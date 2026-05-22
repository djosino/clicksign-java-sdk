package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.MembershipQuery;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.MembershipRole;

import java.util.*;

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

    public String id()                          { return id; }
    public String role()                        { return role; }
    public MembershipRole roleAsEnum() {
        return ApiStringEnum.tryParse(MembershipRole.class, role);
    }
    public boolean consumptionAccessible()      { return consumptionAccessible; }
    public boolean trackingAccessible()         { return trackingAccessible; }
    public boolean folderManagementAccessible() { return folderManagementAccessible; }
    public String userId()                      { return userId; }
    public String createdAt()                   { return createdAt; }
    public String modifiedAt()                  { return modifiedAt; }

    @Override
    public String toString() {
        return "Membership{id='" + id + "', role='" + role + "', userId='" + userId + "'}";
    }

    private static String str(Object o)   { return o != null ? o.toString() : null; }
    private static boolean bool(Object o) { return Boolean.TRUE.equals(o) || "true".equals(str(o)); }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/memberships";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<Membership> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Membership> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) result.add(new Membership(obj));
            return Collections.unmodifiableList(result);
        }

        public MembershipQuery filter() {
            return new MembershipQuery(http);
        }

        public Membership retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Membership(JsonApiParser.parse(raw).firstData());
        }

        public Membership create(CreateParams params) {
            String body = JsonApiSerializer.dump("memberships", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post(ENDPOINT, body);
            return new Membership(JsonApiParser.parse(raw).firstData());
        }

        public Membership update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("memberships", id, params.toAttributes(), null);
            String raw  = http.put(ENDPOINT + "/" + id, body);
            return new Membership(JsonApiParser.parse(raw).firstData());
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

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
            if (consumptionAccessible      != null) m.put("consumption_accessible",       consumptionAccessible);
            if (trackingAccessible         != null) m.put("tracking_accessible",          trackingAccessible);
            if (folderManagementAccessible != null) m.put("folder_management_accessible", folderManagementAccessible);
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

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String role;
            private String userId;
            private Boolean consumptionAccessible;
            private Boolean trackingAccessible;
            private Boolean folderManagementAccessible;

            private Builder() {}

            public Builder role(String v)   { this.role = v; return this; }
            public Builder role(MembershipRole v) {
                return role(v != null ? v.apiValue() : null);
            }
            public Builder userId(String v) { this.userId = v; return this; }
            public Builder consumptionAccessible(boolean v)      { this.consumptionAccessible = v; return this; }
            public Builder trackingAccessible(boolean v)         { this.trackingAccessible = v; return this; }
            public Builder folderManagementAccessible(boolean v) { this.folderManagementAccessible = v; return this; }

            public CreateParams build() {
                if (role == null || role.isBlank())     throw new IllegalArgumentException("role is required");
                if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

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
            if (role                       != null) m.put("role", role);
            if (consumptionAccessible      != null) m.put("consumption_accessible",       consumptionAccessible);
            if (trackingAccessible         != null) m.put("tracking_accessible",          trackingAccessible);
            if (folderManagementAccessible != null) m.put("folder_management_accessible", folderManagementAccessible);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String role;
            private Boolean consumptionAccessible;
            private Boolean trackingAccessible;
            private Boolean folderManagementAccessible;

            private Builder() {}

            public Builder role(String v)                        { this.role = v; return this; }
            public Builder role(MembershipRole v) {
                return role(v != null ? v.apiValue() : null);
            }
            public Builder consumptionAccessible(boolean v)      { this.consumptionAccessible = v; return this; }
            public Builder trackingAccessible(boolean v)         { this.trackingAccessible = v; return this; }
            public Builder folderManagementAccessible(boolean v) { this.folderManagementAccessible = v; return this; }

            public UpdateParams build() { return new UpdateParams(this); }
        }
    }
}
