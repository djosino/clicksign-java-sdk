package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.EventCustomKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Clicksign document event.
 *
 * <p>Events do not support retrieve, update, delete or reload.
 * Use {@link Service#createAddImage} or {@link Service#createCustom} for typed creation.
 */
public final class Event {

    private final String id;
    private final String name;
    private final Map<String, Object> data;
    private final String createdAt;

    private Event(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id        = obj.id();
        this.name      = str(a.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> rawData = (a.get("data") instanceof Map)
            ? Collections.unmodifiableMap((Map<String, Object>) a.get("data"))
            : Collections.emptyMap();
        this.data      = rawData;
        this.createdAt = str(a.get("created"));
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
     * Returns the event name.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the event data map.
     *
     * @return data
     */
    public Map<String, Object> data() {
        return data;
    }

    /**
     * Returns the custom kind as an enum.
     *
     * @return custom kind enum, or {@code null} if not a custom event
     */
    public EventCustomKind customKindAsEnum() {
        Object kind = data != null ? data.get("kind") : null;
        return kind != null ? ApiStringEnum.tryParse(EventCustomKind.class, kind.toString()) : null;
    }

    /**
     * Returns the created at timestamp.
     *
     * @return created at
     */
    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Event{id='" + id + "', name='" + name + "'}";
    }

    static Event fromResource(JsonApiParser.ResourceObject obj, String ignored) {
        return new Event(obj);
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Event operations. */
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
         * Lists all events for an envelope.
         *
         * @param envelopeId envelope id
         * @return unmodifiable list
         */
        public List<Event> listForEnvelope(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/events", Collections.emptyMap());
            List<Event> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Event(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Low-level creation — any event name and data payload.
         *
         * @param params event creation parameters
         * @return created event
         */
        public Event create(CreateParams params) {
            String body = JsonApiSerializer.dump("events", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/documents/" + params.documentId + "/events", body);
            return new Event(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates an add-image event.
         *
         * @param params add-image parameters
         * @return created event
         */
        public Event createAddImage(AddImageParams params) {
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("name", "add_image");
            attrs.put("content_base64", params.contentBase64);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("title", params.title);
            data.put("occurred_at", params.occurredAt);
            attrs.put("data", data);
            String body = JsonApiSerializer.dump("events", null, attrs, null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/documents/" + params.documentId + "/events", body);
            return new Event(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates a custom event.
         *
         * @param params custom event parameters
         * @return created event
         */
        public Event createCustom(CustomParams params) {
            if (ApiStringEnum.tryParse(EventCustomKind.class, params.kind) == null) {
                throw new IllegalArgumentException(
                    "kind must be one of: token_email, token_sms");
            }
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("name", "custom");
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("kind",         params.kind);
            data.put("occurred_at",  params.occurredAt);
            data.put("signer_name",  params.signerName);
            if (params.signerEmail       != null) {
                data.put("signer_email",        params.signerEmail);
            }
            if (params.signerPhoneNumber != null) {
                data.put("signer_phone_number", params.signerPhoneNumber);
            }
            attrs.put("data", data);
            String body = JsonApiSerializer.dump("events", null, attrs, null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/documents/" + params.documentId + "/events", body);
            return new Event(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating a generic event. */
    public static final class CreateParams {

        final String envelopeId;
        final String documentId;
        private final String name;
        private final Map<String, Object> data;
        private final String contentBase64;

        private CreateParams(Builder b) {
            this.envelopeId    = b.envelopeId;
            this.documentId    = b.documentId;
            this.name          = b.name;
            this.data          = b.data != null ? Collections.unmodifiableMap(new LinkedHashMap<>(b.data)) : null;
            this.contentBase64 = b.contentBase64;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            if (data          != null) {
                m.put("data",          data);
            }
            if (contentBase64 != null) {
                m.put("content_base64", contentBase64);
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
            private String documentId;
            private String name;
            private Map<String, Object> data;
            private String contentBase64;

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
             * Sets document id.
             *
             * @param v value
             * @return this builder
             */
            public Builder documentId(String v) {
                this.documentId = v;
                return this;
            }

            /**
             * Sets event name.
             *
             * @param v value
             * @return this builder
             */
            public Builder name(String v) {
                this.name = v;
                return this;
            }

            /**
             * Sets event data.
             *
             * @param v value
             * @return this builder
             */
            public Builder data(Map<String, Object> v) {
                this.data = v;
                return this;
            }

            /**
             * Sets content base64.
             *
             * @param v value
             * @return this builder
             */
            public Builder contentBase64(String v) {
                this.contentBase64 = v;
                return this;
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
                if (documentId == null || documentId.isBlank()) {
                    throw new IllegalArgumentException("documentId is required");
                }
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("name is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── AddImageParams ───────────────────────────────────────────────────────

    /** Parameters for creating an add-image event. */
    public static final class AddImageParams {

        final String envelopeId;
        final String documentId;
        final String title;
        final String occurredAt;
        final String contentBase64;

        private AddImageParams(Builder b) {
            this.envelopeId    = b.envelopeId;
            this.documentId    = b.documentId;
            this.title         = b.title;
            this.occurredAt    = b.occurredAt;
            this.contentBase64 = b.contentBase64;
        }

        /**
         * Returns a new builder.
         *
         * @return new builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link AddImageParams}. */
        public static final class Builder {
            private String envelopeId;
            private String documentId;
            private String title;
            private String occurredAt;
            private String contentBase64;

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
             * Sets document id.
             *
             * @param v value
             * @return this builder
             */
            public Builder documentId(String v) {
                this.documentId = v;
                return this;
            }

            /**
             * Sets title.
             *
             * @param v value
             * @return this builder
             */
            public Builder title(String v) {
                this.title = v;
                return this;
            }

            /**
             * Sets occurred at timestamp.
             *
             * @param v value
             * @return this builder
             */
            public Builder occurredAt(String v) {
                this.occurredAt = v;
                return this;
            }

            /**
             * Sets content base64.
             *
             * @param v value
             * @return this builder
             */
            public Builder contentBase64(String v) {
                this.contentBase64 = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public AddImageParams build() {
                if (envelopeId    == null || envelopeId.isBlank()) {
                    throw new IllegalArgumentException("envelopeId is required");
                }
                if (documentId    == null || documentId.isBlank()) {
                    throw new IllegalArgumentException("documentId is required");
                }
                if (title         == null || title.isBlank()) {
                    throw new IllegalArgumentException("title is required");
                }
                if (occurredAt    == null || occurredAt.isBlank()) {
                    throw new IllegalArgumentException("occurredAt is required");
                }
                if (contentBase64 == null || contentBase64.isBlank()) {
                    throw new IllegalArgumentException("contentBase64 is required");
                }
                return new AddImageParams(this);
            }
        }
    }

    // ── CustomParams ─────────────────────────────────────────────────────────

    /** Parameters for creating a custom signing-evidence event. */
    public static final class CustomParams {

        final String envelopeId;
        final String documentId;
        final String kind;
        final String occurredAt;
        final String signerName;
        final String signerEmail;
        final String signerPhoneNumber;

        private CustomParams(Builder b) {
            this.envelopeId         = b.envelopeId;
            this.documentId         = b.documentId;
            this.kind               = b.kind;
            this.occurredAt         = b.occurredAt;
            this.signerName         = b.signerName;
            this.signerEmail        = b.signerEmail;
            this.signerPhoneNumber  = b.signerPhoneNumber;
        }

        /**
         * Returns a new builder.
         *
         * @return new builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link CustomParams}. */
        public static final class Builder {
            private String envelopeId;
            private String documentId;
            private String kind;
            private String occurredAt;
            private String signerName;
            private String signerEmail;
            private String signerPhoneNumber;

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
             * Sets document id.
             *
             * @param v value
             * @return this builder
             */
            public Builder documentId(String v) {
                this.documentId = v;
                return this;
            }

            /**
             * Sets event kind.
             *
             * @param v value
             * @return this builder
             */
            public Builder kind(String v) {
                this.kind = v;
                return this;
            }

            /**
             * Sets event kind.
             *
             * @param v value
             * @return this builder
             */
            public Builder kind(EventCustomKind v) {
                return kind(v.apiValue());
            }

            /**
             * Sets occurred at timestamp.
             *
             * @param v value
             * @return this builder
             */
            public Builder occurredAt(String v) {
                this.occurredAt = v;
                return this;
            }

            /**
             * Sets signer name.
             *
             * @param v value
             * @return this builder
             */
            public Builder signerName(String v) {
                this.signerName = v;
                return this;
            }

            /**
             * Sets signer email.
             *
             * @param v value
             * @return this builder
             */
            public Builder signerEmail(String v) {
                this.signerEmail = v;
                return this;
            }

            /**
             * Sets signer phone number.
             *
             * @param v value
             * @return this builder
             */
            public Builder signerPhoneNumber(String v) {
                this.signerPhoneNumber = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CustomParams build() {
                if (envelopeId == null || envelopeId.isBlank()) {
                    throw new IllegalArgumentException("envelopeId is required");
                }
                if (documentId == null || documentId.isBlank()) {
                    throw new IllegalArgumentException("documentId is required");
                }
                if (kind == null || kind.isBlank()) {
                    throw new IllegalArgumentException("kind is required");
                }
                if (occurredAt == null || occurredAt.isBlank()) {
                    throw new IllegalArgumentException("occurredAt is required");
                }
                if (signerName == null || signerName.isBlank()) {
                    throw new IllegalArgumentException("signerName is required");
                }
                return new CustomParams(this);
            }
        }
    }
}
