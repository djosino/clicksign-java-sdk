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

    public String id() {
        return id;
    }

    public String endpoint() {
        return endpoint;
    }

    public String status() {
        return status;
    }

    public List<String> events() {
        return events;
    }

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

    public String secret() {
        return secret;
    }

    public String createdAt() {
        return createdAt;
    }

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

    public static final class Service {

        private static final String ENDPOINT = "/webhooks";
        private final HttpClient http;

        public Service(HttpClient http) {
            this.http = http;
        }

        public List<Webhook> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Webhook> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Webhook(obj));
            }
            return Collections.unmodifiableList(result);
        }

        public Webhook retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Webhook(JsonApiParser.parse(raw).firstData());
        }

        public Webhook create(CreateParams params) {
            String body = JsonApiSerializer.dump("webhooks", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new Webhook(JsonApiParser.parse(raw).firstData());
        }

        public Webhook update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("webhooks", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Webhook(JsonApiParser.parse(raw).firstData());
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

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

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String endpoint;
            private List<String> events = new ArrayList<>();
            private String status;
            private String secret;

            private Builder() {}

            public Builder endpoint(String v) {
                this.endpoint = v;
                return this;
            }

            public Builder events(List<String> v) {
                this.events = new ArrayList<>(v);
                return this;
            }

            public Builder addEvent(String v) {
                this.events.add(v);
                return this;
            }

            public Builder addEvent(WebhookEventType v) {
                return addEvent(v.apiValue());
            }

            public Builder status(String v) {
                this.status = v;
                return this;
            }

            public Builder secret(String v) {
                this.secret = v;
                return this;
            }

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

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String endpoint;
            private List<String> events;
            private String status;

            private Builder() {}

            public Builder endpoint(String v) {
                this.endpoint = v;
                return this;
            }

            public Builder events(List<String> v) {
                this.events = new ArrayList<>(v);
                return this;
            }

            public Builder status(String v) {
                this.status = v;
                return this;
            }

            public UpdateParams build() {
                return new UpdateParams(this);
            }
        }
    }
}
