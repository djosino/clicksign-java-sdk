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
     * Returns the kind.
     *
     * @return kind
     */
    public String kind() {
        return kind;
    }

    /**
     * Returns the template id.
     *
     * @return template id
     */
    public String templateId() {
        return templateId;
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

    /** HTTP service for TemplateField operations. */
    public static final class Service {

        private static final String ENDPOINT = "/template_fields";
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
        public List<TemplateField> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<TemplateField> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new TemplateField(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Updates resource.
         *
         * @param id         resource id
         * @param templateId parent template id
         * @param params     update parameters
         * @return updated resource
         */
        public TemplateField update(String id, String templateId, UpdateParams params) {
            String body = JsonApiSerializer.dump("template_fields", id, params.toAttributes(), null);
            String raw  = http.patch("/templates/" + templateId + "/template_fields/" + id, body);
            return new TemplateField(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Deletes resource by id.
         *
         * @param id         resource id
         * @param templateId parent template id
         */
        public void delete(String id, String templateId) {
            http.delete("/templates/" + templateId + "/template_fields/" + id, null);
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating a template field. */
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
