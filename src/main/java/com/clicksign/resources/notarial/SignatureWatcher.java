package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.SignatureWatcherKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Returns the id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Returns the email.
     *
     * @return email
     */
    public String email() {
        return email;
    }

    /**
     * Returns the kind.
     *
     * @return kind
     */
    public String kind() {
        return kind;
    }

    /**
     * Returns the kind as an enum.
     *
     * @return kind enum
     */
    public SignatureWatcherKind kindAsEnum() {
        return ApiStringEnum.tryParse(SignatureWatcherKind.class, kind);
    }

    /**
     * Returns whether attach documents is enabled.
     *
     * @return attach documents enabled flag
     */
    public boolean attachDocumentsEnabled() {
        return attachDocumentsEnabled;
    }

    /**
     * Returns the communicate events map.
     *
     * @return communicate events map
     */
    public Map<String, Object> communicateEvents() {
        return communicateEvents;
    }

    /**
     * Parsed view of {@link #communicateEvents()}; {@code null} when unset.
     *
     * @return parsed communicate events, or {@code null}
     */
    public CommunicateEvents communicateEventsConfig() {
        return CommunicateEvents.fromMap(communicateEvents);
    }

    /**
     * Returns the envelope id.
     *
     * @return envelope id
     */
    public String envelopeId() {
        return envelopeId;
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
        return "SignatureWatcher{id='" + id + "', email='" + email + "', envelopeId='" + envelopeId + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static boolean bool(Object o) {
        return Boolean.TRUE.equals(o) || "true".equals(str(o));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for SignatureWatcher operations. */
    public static final class Service {

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
         * Lists all resources for the given envelope.
         *
         * @param envelopeId envelope id
         * @return unmodifiable list
         */
        public List<SignatureWatcher> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signature_watchers", Collections.emptyMap());
            List<SignatureWatcher> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new SignatureWatcher(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id         resource id
         * @param envelopeId envelope id
         * @return resource
         */
        public SignatureWatcher retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signature_watchers/" + id, Collections.emptyMap());
            return new SignatureWatcher(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public SignatureWatcher create(CreateParams params) {
            String body = JsonApiSerializer.dump("signature_watchers", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/signature_watchers", body);
            return new SignatureWatcher(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        /**
         * Deletes resource by id.
         *
         * @param id         resource id
         * @param envelopeId envelope id
         */
        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/signature_watchers/" + id, null);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating a signature watcher. */
    public static final class CreateParams {

        private final String envelopeId;
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
            if (attachDocumentsEnabled != null) {
                m.put("attach_documents_enabled", attachDocumentsEnabled);
            }
            if (communicateEvents      != null) {
                m.put("communicate_events",         communicateEvents);
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

        /** Builder for {@link CreateParams}. */
        public static final class Builder {
            private String envelopeId;
            private String email;
            private String kind;
            private Boolean attachDocumentsEnabled;
            private Map<String, Object> communicateEvents;

            private Builder() {}

            /**
             * Sets envelope id.
             *
             * @param v value
             * @return this builder
             */
            public Builder envelopeId(String v) {
                this.envelopeId = v;
                return this;
            }

            /**
             * Sets email.
             *
             * @param v value
             * @return this builder
             */
            public Builder email(String v) {
                this.email = v;
                return this;
            }

            /**
             * Sets kind.
             *
             * @param v value
             * @return this builder
             */
            public Builder kind(String v) {
                this.kind = v;
                return this;
            }

            /**
             * Sets kind.
             *
             * @param v value
             * @return this builder
             */
            public Builder kind(SignatureWatcherKind v) {
                return kind(v != null ? v.apiValue() : null);
            }

            /**
             * Sets attach documents enabled flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder attachDocumentsEnabled(boolean v) {
                this.attachDocumentsEnabled = v;
                return this;
            }

            /**
             * Sets communicate events.
             *
             * @param v value
             * @return this builder
             */
            public Builder communicateEvents(Map<String, Object> v) {
                this.communicateEvents = v;
                return this;
            }

            /**
             * Sets communicate events.
             *
             * @param v value
             * @return this builder
             */
            public Builder communicateEvents(CommunicateEvents v) {
                return communicateEvents(v != null ? v.toMap() : null);
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (envelopeId == null || envelopeId.isBlank()) {
                    throw new IllegalArgumentException("envelopeId is required");
                }
                if (email == null || email.isBlank()) {
                    throw new IllegalArgumentException("email is required");
                }
                if (kind == null || kind.isBlank()) {
                    throw new IllegalArgumentException("kind is required");
                }
                return new CreateParams(this);
            }
        }
    }
}
