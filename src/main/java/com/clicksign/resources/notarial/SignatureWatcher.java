package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.SignatureWatcherKind;

import java.util.*;

/** Represents a Clicksign signature watcher. Does not support update. */
public final class SignatureWatcher {

    private final String id;
    private final String email;
    private final String kind;
    private final boolean attachDocumentsEnabled;
    private final Map<String, Object> communicateEvents;
    private final String envelopeId;
    private final String createdAt;
    private final String modifiedAt;

    @SuppressWarnings("unchecked")
    private SignatureWatcher(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        Map<String, Object> a = obj.attributes();
        this.id                      = obj.id();
        this.email                   = str(a.get("email"));
        this.kind                    = str(a.get("kind"));
        this.attachDocumentsEnabled  = bool(a.get("attach_documents_enabled"));
        this.communicateEvents       = objectMap(a.get("communicate_events"));
        this.envelopeId              = parentEnvelopeId != null ? parentEnvelopeId : obj.relationshipId("envelope");
        this.createdAt               = str(a.get("created"));
        this.modifiedAt              = str(a.get("modified"));
    }

    public String id()                      { return id; }
    public String email()                   { return email; }
    public String kind()                    { return kind; }
    public SignatureWatcherKind kindAsEnum() { return ApiStringEnum.tryParse(SignatureWatcherKind.class, kind); }
    public boolean attachDocumentsEnabled() { return attachDocumentsEnabled; }
    public Map<String, Object> communicateEvents() { return communicateEvents; }

    /** Parsed view of {@link #communicateEvents()}; {@code null} when unset. */
    public CommunicateEvents communicateEventsConfig() {
        return CommunicateEvents.fromMap(communicateEvents);
    }
    public String envelopeId()              { return envelopeId; }
    public String createdAt()               { return createdAt; }
    public String modifiedAt()              { return modifiedAt; }

    @Override
    public String toString() {
        return "SignatureWatcher{id='" + id + "', email='" + email + "', envelopeId='" + envelopeId + "'}";
    }

    private static String str(Object o)   { return o != null ? o.toString() : null; }
    private static boolean bool(Object o) { return Boolean.TRUE.equals(o) || "true".equals(str(o)); }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) return null;
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<SignatureWatcher> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signature_watchers", Collections.emptyMap());
            List<SignatureWatcher> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new SignatureWatcher(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        public SignatureWatcher retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signature_watchers/" + id, Collections.emptyMap());
            return new SignatureWatcher(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        public SignatureWatcher create(CreateParams params) {
            String body = JsonApiSerializer.dump("signature_watchers", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/signature_watchers", body);
            return new SignatureWatcher(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/signature_watchers/" + id, null);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        final String envelopeId;
        private final String email;
        private final String kind;
        private final Boolean attachDocumentsEnabled;
        private final Map<String, Object> communicateEvents;

        private CreateParams(Builder b) {
            this.envelopeId             = b.envelopeId;
            this.email                  = b.email;
            this.kind                   = b.kind;
            this.attachDocumentsEnabled = b.attachDocumentsEnabled;
            this.communicateEvents      = b.communicateEvents != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.communicateEvents)) : null;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("email", email);
            m.put("kind",  kind);
            if (attachDocumentsEnabled != null) m.put("attach_documents_enabled", attachDocumentsEnabled);
            if (communicateEvents      != null) m.put("communicate_events",         communicateEvents);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String envelopeId;
            private String email;
            private String kind;
            private Boolean attachDocumentsEnabled;
            private Map<String, Object> communicateEvents;

            private Builder() {}

            public Builder envelopeId(String v)              { this.envelopeId = v; return this; }
            public Builder email(String v)                   { this.email = v; return this; }
            public Builder kind(String v)                    { this.kind = v; return this; }
            public Builder kind(SignatureWatcherKind v)      { return kind(v.apiValue()); }
            public Builder attachDocumentsEnabled(boolean v) { this.attachDocumentsEnabled = v; return this; }
            public Builder communicateEvents(Map<String, Object> v) { this.communicateEvents = v; return this; }

            public Builder communicateEvents(CommunicateEvents v) {
                return communicateEvents(v != null ? v.toMap() : null);
            }

            public CreateParams build() {
                if (envelopeId == null || envelopeId.isBlank()) throw new IllegalArgumentException("envelopeId is required");
                if (email == null || email.isBlank())           throw new IllegalArgumentException("email is required");
                if (kind == null || kind.isBlank())             throw new IllegalArgumentException("kind is required");
                return new CreateParams(this);
            }
        }
    }
}
