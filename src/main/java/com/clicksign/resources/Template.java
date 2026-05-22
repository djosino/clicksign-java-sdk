package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.*;

/** Represents a Clicksign template. */
public final class Template {

    private final String id;
    private final String name;
    private final String color;
    private final String createdAt;
    private final String modifiedAt;

    private Template(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id         = obj.id();
        this.name       = str(a.get("name"));
        this.color      = str(a.get("color"));
        this.createdAt  = str(a.get("created"));
        this.modifiedAt = str(a.get("modified"));
    }

    public String id()          { return id; }
    public String name()        { return name; }
    public String color()       { return color; }
    public String createdAt()   { return createdAt; }
    public String modifiedAt()  { return modifiedAt; }

    @Override
    public String toString() {
        return "Template{id='" + id + "', name='" + name + "'}";
    }

    private static String str(Object o) { return o != null ? o.toString() : null; }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/templates";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<Template> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Template> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) result.add(new Template(obj));
            return Collections.unmodifiableList(result);
        }

        public Template retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Template(JsonApiParser.parse(raw).firstData());
        }

        public Template create(CreateParams params) {
            String body = JsonApiSerializer.dump("templates", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new Template(JsonApiParser.parse(raw).firstData());
        }

        public Template update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("templates", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Template(JsonApiParser.parse(raw).firstData());
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }

        public List<TemplateField> listTemplateFields(String templateId) {
            String raw = http.get(ENDPOINT + "/" + templateId + "/template_fields", Collections.emptyMap());
            List<TemplateField> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(TemplateField.fromResource(obj));
            }
            return Collections.unmodifiableList(result);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        private final String name;
        private final String color;
        private final String contentBase64;

        private CreateParams(Builder b) {
            this.name          = b.name;
            this.color         = b.color;
            this.contentBase64 = b.contentBase64;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("content_base64", contentBase64);
            if (color != null) m.put("color", color);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;
            private String color;
            private String contentBase64;

            private Builder() {}

            public Builder name(String v)            { this.name = v; return this; }
            public Builder color(String v)           { this.color = v; return this; }
            public Builder contentBase64(String v)   { this.contentBase64 = v; return this; }

            public CreateParams build() {
                if (name == null || name.isBlank())             throw new IllegalArgumentException("name is required");
                if (contentBase64 == null || contentBase64.isBlank())
                    throw new IllegalArgumentException("contentBase64 is required");
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    public static final class UpdateParams {

        private final String name;
        private final String color;

        private UpdateParams(Builder b) {
            this.name  = b.name;
            this.color = b.color;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name  != null) m.put("name",  name);
            if (color != null) m.put("color", color);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;
            private String color;

            private Builder() {}

            public Builder name(String v)  { this.name = v; return this; }
            public Builder color(String v) { this.color = v; return this; }

            public UpdateParams build() { return new UpdateParams(this); }
        }
    }
}
