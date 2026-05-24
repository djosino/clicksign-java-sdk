package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.WebhookEventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a Clicksign webhook. Full CRUD supported. */
public final class Webhook {

    private final String id;
    private final String endpoint;
    private final String status;
    private final List<String> events;
    private final String secret;
    private final String createdAt;
    private final String modifiedAt;

    private Webhook(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id         = obj.id();
        this.endpoint   = str(a.get("endpoint"));
        this.status     = str(a.get("status"));
        this.secret     = str(a.get("secret"));
        this.createdAt  = str(a.get("created"));
        this.modifiedAt = str(a.get("modified"));

        Object rawEvents = a.get("events");
        List<String> ev = new ArrayList<>();
        if (rawEvents instanceof List) {
            for (Object e : (List<?>) rawEvents) {
                ev.add(e.toString());
            }
        }
        this.events = Collections.unmodifiableList(ev);
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
     * Returns the endpoint URL.
     *
     * @return endpoint
     */
    public String endpoint() {
        return endpoint;
    }

    /**
     * Returns the status.
     *
     * @return status
     */
    public String status() {
        return status;
    }

    /**
     * Returns the subscribed event types.
     *
     * @return events
     */
    public List<String> events() {
        return events;
    }

    /**
     * Returns the subscribed event types as enums.
     *
     * @return events as enums
     */
    public List<WebhookEventType> eventsAsEnums() {
        List<WebhookEventType> parsed = new ArrayList<>();
        for (String e : events) {
            WebhookEventType type = ApiStringEnum.tryParse(WebhookEventType.class, e);
            if (type != null) {
                parsed.add(type);
            }
        }
        return Collections.unmodifiableList(parsed);
    }

    /**
     * Returns the secret.
     *
     * @return secret
     */
    public String secret() {
        return secret;
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
        return "Webhook{id='" + id + "', endpoint='" + endpoint + "', status='" + status + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Webhook operations. */
    public static final class Service {

        private static final String ENDPOINT = "/webhooks";
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
        public List<Webhook> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Webhook> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Webhook(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id resource id
         * @return resource
         */
        public Webhook retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Webhook(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public Webhook create(CreateParams params) {
            String body = JsonApiSerializer.dump("webhooks", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new Webhook(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Updates resource.
         *
         * @param id     resource id
         * @param params update parameters
         * @return updated resource
         */
        public Webhook update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("webhooks", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Webhook(JsonApiParser.parse(raw).firstData());
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

    /** Parameters for creating a webhook. */
    public static final class CreateParams {

        private final String endpoint;
        private final List<String> events;
        private final String status;
        private final String secret;

        private CreateParams(Builder b) {
            this.endpoint = b.endpoint;
            this.events   = Collections.unmodifiableList(new ArrayList<>(b.events));
            this.status   = b.status;
            this.secret   = b.secret;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("endpoint", endpoint);
            m.put("events",   events);
            if (status != null) {
                m.put("status", status);
            }
            if (secret != null) {
                m.put("secret", secret);
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
            private String endpoint;
            private List<String> events = new ArrayList<>();
            private String status;
            private String secret;

            private Builder() {}

            /**
             * Sets endpoint URL.
             *
             * @param v value
             * @return this builder
             */
            public Builder endpoint(String v) {
                this.endpoint = v;
                return this;
            }

            /**
             * Sets event types.
             *
             * @param v value
             * @return this builder
             */
            public Builder events(List<String> v) {
                this.events = new ArrayList<>(v);
                return this;
            }

            /**
             * Adds an event type.
             *
             * @param v value
             * @return this builder
             */
            public Builder addEvent(String v) {
                this.events.add(v);
                return this;
            }

            /**
             * Adds an event type.
             *
             * @param v value
             * @return this builder
             */
            public Builder addEvent(WebhookEventType v) {
                return addEvent(v != null ? v.apiValue() : null);
            }

            /**
             * Sets status.
             *
             * @param v value
             * @return this builder
             */
            public Builder status(String v) {
                this.status = v;
                return this;
            }

            /**
             * Sets secret.
             *
             * @param v value
             * @return this builder
             */
            public Builder secret(String v) {
                this.secret = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (endpoint == null || endpoint.isBlank()) {
                    throw new IllegalArgumentException("endpoint is required");
                }
                if (events.isEmpty()) {
                    throw new IllegalArgumentException("at least one event is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating a webhook. */
    public static final class UpdateParams {

        private final String endpoint;
        private final List<String> events;
        private final String status;

        private UpdateParams(Builder b) {
            this.endpoint = b.endpoint;
            this.events   = b.events != null ? Collections.unmodifiableList(new ArrayList<>(b.events)) : null;
            this.status   = b.status;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (endpoint != null) {
                m.put("endpoint", endpoint);
            }
            if (events   != null) {
                m.put("events",   events);
            }
            if (status   != null) {
                m.put("status",   status);
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
            private String endpoint;
            private List<String> events;
            private String status;

            private Builder() {}

            /**
             * Sets endpoint URL.
             *
             * @param v value
             * @return this builder
             */
            public Builder endpoint(String v) {
                this.endpoint = v;
                return this;
            }

            /**
             * Sets event types.
             *
             * @param v value
             * @return this builder
             */
            public Builder events(List<String> v) {
                this.events = new ArrayList<>(v);
                return this;
            }

            /**
             * Sets status.
             *
             * @param v value
             * @return this builder
             */
            public Builder status(String v) {
                this.status = v;
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
