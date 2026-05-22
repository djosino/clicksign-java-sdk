package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Returns the id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Returns the name.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the color.
     *
     * @return color
     */
    public String color() {
        return color;
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
        return "Template{id='" + id + "', name='" + name + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Template operations. */
    public static final class Service {

        private static final String ENDPOINT = "/templates";
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
        public List<Template> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Template> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Template(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id resource id
         * @return resource
         */
        public Template retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Template(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public Template create(CreateParams params) {
            String body = JsonApiSerializer.dump("templates", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new Template(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Updates resource.
         *
         * @param id     resource id
         * @param params update parameters
         * @return updated resource
         */
        public Template update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("templates", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Template(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Deletes resource by id.
         *
         * @param id resource id
         */
        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }

        /**
         * Lists template fields for the given template.
         *
         * @param templateId template id
         * @return unmodifiable list
         */
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

    /** Parameters for creating a template. */
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
            if (color != null) {
                m.put("color", color);
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
            private String name;
            private String color;
            private String contentBase64;

            private Builder() {}

            /**
             * Sets name.
             *
             * @param v value
             * @return this builder
             */
            public Builder name(String v) {
                this.name = v;
                return this;
            }

            /**
             * Sets color.
             *
             * @param v value
             * @return this builder
             */
            public Builder color(String v) {
                this.color = v;
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
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("name is required");
                }
                if (contentBase64 == null || contentBase64.isBlank()) {
                    throw new IllegalArgumentException("contentBase64 is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating a template. */
    public static final class UpdateParams {

        private final String name;
        private final String color;

        private UpdateParams(Builder b) {
            this.name  = b.name;
            this.color = b.color;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name  != null) {
                m.put("name",  name);
            }
            if (color != null) {
                m.put("color", color);
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
            private String name;
            private String color;

            private Builder() {}

            /**
             * Sets name.
             *
             * @param v value
             * @return this builder
             */
            public Builder name(String v) {
                this.name = v;
                return this;
            }

            /**
             * Sets color.
             *
             * @param v value
             * @return this builder
             */
            public Builder color(String v) {
                this.color = v;
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
