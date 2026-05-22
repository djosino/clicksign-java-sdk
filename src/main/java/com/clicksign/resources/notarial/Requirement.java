package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.RequirementQuery;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.RequirementAction;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RubricateKind;

import java.util.*;

/** Represents a signing requirement linking a signer to a document action. */
public final class Requirement {

    private final String id;
    private final String action;
    private final String role;
    private final String auth;
    private final String pages;
    private final String kind;
    private final String rubricField;
    private final String envelopeId;
    private final String documentId;
    private final String signerId;
    private final String createdAt;
    private final String modifiedAt;

    public static Requirement from(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        return new Requirement(obj, parentEnvelopeId);
    }

    private Requirement(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        Map<String, Object> a = obj.attributes();
        this.id          = obj.id();
        this.action      = str(a.get("action"));
        this.role        = str(a.get("role"));
        this.auth        = str(a.get("auth"));
        this.pages       = str(a.get("pages"));
        this.kind        = str(a.get("kind"));
        this.rubricField = str(a.get("rubric_field"));
        this.envelopeId  = parentEnvelopeId != null ? parentEnvelopeId : obj.relationshipId("envelope");
        this.documentId  = obj.relationshipId("document");
        this.signerId    = obj.relationshipId("signer");
        this.createdAt   = str(a.get("created"));
        this.modifiedAt  = str(a.get("modified"));
    }

    public String id()           { return id; }
    public String action()       { return action; }
    public String role()         { return role; }
    public String auth()         { return auth; }
    public String pages()        { return pages; }
    public String kind()         { return kind; }
    public String rubricField()  { return rubricField; }
    public String envelopeId()   { return envelopeId; }
    public String documentId()   { return documentId; }
    public String signerId()     { return signerId; }
    public RequirementAction actionAsEnum() { return ApiStringEnum.tryParse(RequirementAction.class, action); }
    public RequirementRole roleAsEnum()     { return ApiStringEnum.tryParse(RequirementRole.class, role); }
    public RequirementAuth authAsEnum()     { return ApiStringEnum.tryParse(RequirementAuth.class, auth); }
    public RubricateKind kindAsEnum()       { return ApiStringEnum.tryParse(RubricateKind.class, kind); }
    public String createdAt()    { return createdAt; }
    public String modifiedAt()   { return modifiedAt; }

    @Override
    public String toString() {
        return "Requirement{id='" + id + "', action='" + action + "', envelopeId='" + envelopeId + "'}";
    }

    static Requirement fromResource(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        return new Requirement(obj, parentEnvelopeId);
    }

    private static String str(Object o) { return o != null ? o.toString() : null; }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public Requirement retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/requirements/" + id, Collections.emptyMap());
            return new Requirement(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        public Requirement create(CreateParams params) {
            String body = JsonApiSerializer.dump("requirements", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post("/envelopes/" + params.envelopeId + "/requirements", body);
            return new Requirement(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        public Requirement update(String id, String envelopeId, UpdateParams params) {
            String body = JsonApiSerializer.dump("requirements", id, params.toAttributes(), null);
            String raw  = http.patch("/envelopes/" + envelopeId + "/requirements/" + id, body);
            return new Requirement(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/requirements/" + id, null);
        }

        public List<Requirement> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/requirements", Collections.emptyMap());
            List<Requirement> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Requirement(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        /** Returns a fluent query builder for filtering and paginating requirements. */
        public RequirementQuery filter(String envelopeId) {
            return new RequirementQuery(envelopeId, http);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        final String envelopeId;
        private final String action;
        private final String role;
        private final String auth;
        private final String pages;
        private final String kind;
        private final String rubricField;
        private final String documentId;
        private final String signerId;

        private CreateParams(Builder b) {
            this.envelopeId  = b.envelopeId;
            this.action      = b.action;
            this.role        = b.role;
            this.auth        = b.auth;
            this.pages       = b.pages;
            this.kind        = b.kind;
            this.rubricField = b.rubricField;
            this.documentId  = b.documentId;
            this.signerId    = b.signerId;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("action", action);
            if (role        != null) m.put("role",         role);
            if (auth        != null) m.put("auth",         auth);
            if (pages       != null) m.put("pages",        pages);
            if (kind        != null) m.put("kind",         kind);
            if (rubricField != null) m.put("rubric_field", rubricField);
            return m;
        }

        Map<String, Object> toRelationships() {
            Map<String, Object> rels = new LinkedHashMap<>();
            if (documentId != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("type", "documents");
                data.put("id", documentId);
                Map<String, Object> rel = new LinkedHashMap<>();
                rel.put("data", data);
                rels.put("document", rel);
            }
            if (signerId != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("type", "signers");
                data.put("id", signerId);
                Map<String, Object> rel = new LinkedHashMap<>();
                rel.put("data", data);
                rels.put("signer", rel);
            }
            return rels;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String envelopeId;
            private String action;
            private String role;
            private String auth;
            private String pages;
            private String kind;
            private String rubricField;
            private String documentId;
            private String signerId;

            private Builder() {}

            public Builder envelopeId(String v)  { this.envelopeId = v; return this; }
            public Builder action(String v)       { this.action = v; return this; }
            public Builder action(RequirementAction v) { return action(v.apiValue()); }
            public Builder role(String v)         { this.role = v; return this; }
            public Builder role(RequirementRole v)   { return role(v.apiValue()); }
            public Builder auth(String v)         { this.auth = v; return this; }
            public Builder auth(RequirementAuth v)     { return auth(v.apiValue()); }
            public Builder pages(String v)        { this.pages = v; return this; }
            public Builder kind(String v)         { this.kind = v; return this; }
            public Builder kind(RubricateKind v)       { return kind(v.apiValue()); }
            public Builder rubricField(String v)  { this.rubricField = v; return this; }
            public Builder documentId(String v)   { this.documentId = v; return this; }
            public Builder signerId(String v)     { this.signerId = v; return this; }

            public CreateParams build() {
                if (envelopeId == null || envelopeId.isBlank()) throw new IllegalArgumentException("envelopeId is required");
                if (action == null || action.isBlank())         throw new IllegalArgumentException("action is required");
                if (RequirementAction.RUBRICATE.apiValue().equals(action)
                    && (pages == null || pages.isBlank())
                    && (rubricField == null || rubricField.isBlank())) {
                    throw new IllegalArgumentException(
                        "pages or rubricField is required when action is rubricate");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    public static final class UpdateParams {

        private final String action;
        private final String role;
        private final String auth;

        private UpdateParams(Builder b) {
            this.action = b.action;
            this.role   = b.role;
            this.auth   = b.auth;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (action != null) m.put("action", action);
            if (role   != null) m.put("role",   role);
            if (auth   != null) m.put("auth",   auth);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String action;
            private String role;
            private String auth;

            private Builder() {}

            public Builder action(String v) { this.action = v; return this; }
            public Builder action(RequirementAction v) { return action(v.apiValue()); }
            public Builder role(String v)   { this.role = v; return this; }
            public Builder role(RequirementRole v)   { return role(v.apiValue()); }
            public Builder auth(String v)   { this.auth = v; return this; }
            public Builder auth(RequirementAuth v)   { return auth(v.apiValue()); }

            public UpdateParams build() { return new UpdateParams(this); }
        }
    }
}
