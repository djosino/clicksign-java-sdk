package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a field within a Clicksign template. */
public final class TemplateField {

    private final String id;
    private final String name;
    private final String kind;
    private final String templateId;
    private final String createdAt;
    private final String modifiedAt;

    private TemplateField(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id         = obj.id();
        this.name       = str(a.get("name"));
        this.kind       = str(a.get("kind"));
        this.templateId = obj.relationshipId("template");
        this.createdAt  = str(a.get("created"));
        this.modifiedAt = str(a.get("modified"));
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String kind() {
        return kind;
    }

    public String templateId() {
        return templateId;
    }

    public String createdAt() {
        return createdAt;
    }

    public String modifiedAt() {
        return modifiedAt;
    }

    static TemplateField fromResource(JsonApiParser.ResourceObject obj) {
        return new TemplateField(obj);
    }

    @Override
    public String toString() {
        return "TemplateField{id='" + id + "', name='" + name + "', kind='" + kind + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/template_fields";
        private final HttpClient http;

        public Service(HttpClient http) {
            this.http = http;
        }

        public List<TemplateField> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<TemplateField> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new TemplateField(obj));
            }
            return Collections.unmodifiableList(result);
        }

        public TemplateField update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("template_fields", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new TemplateField(JsonApiParser.parse(raw).firstData());
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

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

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String name;

            private Builder() {}

            public Builder name(String v) {
                this.name = v;
                return this;
            }

            public UpdateParams build() {
                return new UpdateParams(this);
            }
        }
    }
}
