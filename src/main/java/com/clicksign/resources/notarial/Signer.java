package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.ResourceQuery;

import java.util.*;

/** Represents a Clicksign signer. Signers do not support update. */
public final class Signer {

    private final String id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String birthday;
    private final boolean refusable;
    private final boolean locationRequiredEnabled;
    private final boolean hasDocumentation;
    private final String documentation;
    private final SignatureHost signatureHost;
    private final Map<String, Object> communicateEvents;
    private final Integer group;
    private final String envelopeId;
    private final String createdAt;
    private final String modifiedAt;

    @SuppressWarnings("unchecked")
    private Signer(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        Map<String, Object> a = obj.attributes();
        this.id                      = obj.id();
        this.name                    = str(a.get("name"));
        this.email                   = str(a.get("email"));
        this.phoneNumber             = str(a.get("phone_number"));
        this.birthday                = str(a.get("birthday"));
        this.refusable               = bool(a.get("refusable"));
        this.locationRequiredEnabled = bool(a.get("location_required_enabled"));
        this.hasDocumentation        = bool(a.get("has_documentation"));
        this.documentation           = str(a.get("documentation"));
        this.signatureHost           = parseSignatureHost(a.get("signature_host"));
        this.communicateEvents       = objectMap(a.get("communicate_events"));
        this.group                   = integer(a.get("group"));
        this.envelopeId              = parentEnvelopeId != null ? parentEnvelopeId : obj.relationshipId("envelope");
        this.createdAt               = str(a.get("created"));
        this.modifiedAt              = str(a.get("modified"));
    }

    public String id()                       { return id; }
    public String name()                     { return name; }
    public String email()                    { return email; }
    public String phoneNumber()              { return phoneNumber; }
    public String birthday()                 { return birthday; }
    public boolean refusable()               { return refusable; }
    public boolean locationRequiredEnabled() { return locationRequiredEnabled; }
    public boolean hasDocumentation()        { return hasDocumentation; }
    public String documentation()            { return documentation; }
    public SignatureHost signatureHost()     { return signatureHost; }
    public Map<String, Object> communicateEvents() { return communicateEvents; }

    /** Parsed view of {@link #communicateEvents()}; {@code null} when unset. */
    public CommunicateEvents communicateEventsConfig() {
        return CommunicateEvents.fromMap(communicateEvents);
    }

    public Integer group()                   { return group; }
    public String envelopeId()               { return envelopeId; }
    public String createdAt()                { return createdAt; }
    public String modifiedAt()               { return modifiedAt; }

    @Override
    public String toString() {
        return "Signer{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }

    private static String str(Object o)   { return o != null ? o.toString() : null; }
    private static boolean bool(Object o) { return Boolean.TRUE.equals(o) || "true".equals(str(o)); }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) return null;
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    private static Integer integer(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static SignatureHost parseSignatureHost(Object o) {
        if (!(o instanceof Map)) return null;
        Map<String, Object> m = (Map<String, Object>) o;
        return new SignatureHost(
            str(m.get("name")),
            str(m.get("email")),
            objectMap(m.get("communicate_events")));
    }

    /** Signature host with its own communication preferences. */
    public static final class SignatureHost {

        private final String name;
        private final String email;
        private final Map<String, Object> communicateEvents;

        public SignatureHost(String name, String email, Map<String, Object> communicateEvents) {
            this.name = name;
            this.email = email;
            this.communicateEvents = communicateEvents != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(communicateEvents)) : null;
        }

        public SignatureHost(String name, String email, CommunicateEvents communicateEvents) {
            this(name, email, communicateEvents != null ? communicateEvents.toMap() : null);
        }

        public String name() { return name; }
        public String email() { return email; }
        public Map<String, Object> communicateEvents() { return communicateEvents; }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name  != null) m.put("name",  name);
            if (email != null) m.put("email", email);
            if (communicateEvents != null) m.put("communicate_events", communicateEvents);
            return m;
        }
    }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<Signer> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signers", Collections.emptyMap());
            List<Signer> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Signer(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        /** Returns a fluent query builder for filtering and paginating signers. */
        public ResourceQuery<Signer> filter(String envelopeId) {
            return new ResourceQuery<>("/envelopes/" + envelopeId + "/signers", http,
                obj -> new Signer(obj, envelopeId));
        }

        public Signer retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signers/" + id, Collections.emptyMap());
            return new Signer(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        public Signer create(CreateParams params) {
            String body = JsonApiSerializer.dump("signers", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/signers", body);
            return new Signer(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/signers/" + id, null);
        }

        public void notify(String id, String envelopeId, NotificationParams params) {
            String body = JsonApiSerializer.dump("notifications", null, params.toAttributes(), null);
            http.post("/envelopes/" + envelopeId + "/signers/" + id + "/notifications", body);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        final String envelopeId;
        private final String name;
        private final String email;
        private final String phoneNumber;
        private final String birthday;
        private final Boolean refusable;
        private final Boolean locationRequiredEnabled;
        private final Boolean hasDocumentation;
        private final String documentation;
        private final SignatureHost signatureHost;
        private final Map<String, Object> communicateEvents;
        private final Integer group;

        private CreateParams(Builder b) {
            this.envelopeId              = b.envelopeId;
            this.name                    = b.name;
            this.email                   = b.email;
            this.phoneNumber             = b.phoneNumber;
            this.birthday                = b.birthday;
            this.refusable               = b.refusable;
            this.locationRequiredEnabled = b.locationRequiredEnabled;
            this.hasDocumentation        = b.hasDocumentation;
            this.documentation           = b.documentation;
            this.signatureHost           = b.signatureHost;
            this.communicateEvents       = b.communicateEvents != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.communicateEvents)) : null;
            this.group                   = b.group;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name  != null) m.put("name",  name);
            if (email != null) m.put("email", email);
            if (phoneNumber != null) m.put("phone_number", phoneNumber);
            if (birthday != null) m.put("birthday", birthday);
            if (refusable != null) m.put("refusable", refusable);
            if (locationRequiredEnabled != null) m.put("location_required_enabled", locationRequiredEnabled);
            if (hasDocumentation != null) m.put("has_documentation", hasDocumentation);
            if (documentation != null) m.put("documentation", documentation);
            if (signatureHost != null) m.put("signature_host", signatureHost.toAttributes());
            if (communicateEvents != null) m.put("communicate_events", communicateEvents);
            if (group != null) m.put("group", group);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String envelopeId;
            private String name;
            private String email;
            private String phoneNumber;
            private String birthday;
            private Boolean refusable;
            private Boolean locationRequiredEnabled;
            private Boolean hasDocumentation;
            private String documentation;
            private SignatureHost signatureHost;
            private Map<String, Object> communicateEvents;
            private Integer group;

            private Builder() {}

            public Builder envelopeId(String v)               { this.envelopeId = v; return this; }
            public Builder name(String v)                     { this.name = v; return this; }
            public Builder email(String v)                    { this.email = v; return this; }
            public Builder phoneNumber(String v)              { this.phoneNumber = v; return this; }
            public Builder birthday(String v)                 { this.birthday = v; return this; }
            public Builder refusable(boolean v)               { this.refusable = v; return this; }
            public Builder locationRequiredEnabled(boolean v) { this.locationRequiredEnabled = v; return this; }
            public Builder hasDocumentation(boolean v)        { this.hasDocumentation = v; return this; }
            public Builder documentation(String v)            { this.documentation = v; return this; }
            public Builder signatureHost(SignatureHost v)     { this.signatureHost = v; return this; }
            public Builder communicateEvents(Map<String, Object> v) { this.communicateEvents = v; return this; }

            public Builder communicateEvents(CommunicateEvents v) {
                return communicateEvents(v != null ? v.toMap() : null);
            }

            public Builder group(int v)                       { this.group = v; return this; }

            public CreateParams build() {
                if (envelopeId == null || envelopeId.isBlank()) throw new IllegalArgumentException("envelopeId is required");
                if (name == null || name.isBlank())             throw new IllegalArgumentException("name is required");
                if (email == null || email.isBlank())           throw new IllegalArgumentException("email is required");
                SignerValidation.validateName(name);
                SignerValidation.validateOptionalFields(
                    hasDocumentation, documentation, birthday, phoneNumber, communicateEvents);
                return new CreateParams(this);
            }
        }
    }

}
