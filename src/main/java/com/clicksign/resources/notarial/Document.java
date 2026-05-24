package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.DocumentQuery;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.DocumentDuplicate;
import com.clicksign.resources.types.DocumentStatus;
import com.clicksign.resources.types.DocumentTemplate;
import com.clicksign.resources.types.Metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Clicksign document within an envelope.
 *
 * <pre>{@code
 * Document doc = client.documents().create(
 *     Document.CreateParams.builder()
 *         .envelopeId(envelope.id())
 *         .filename("contrato.pdf")
 *         .contentBase64("data:application/pdf;base64,...")
 *         .build()
 * );
 * }</pre>
 */
public final class Document {

    private final String id;
    private final String filename;
    private final String status;
    private final Map<String, Object> metadata;
    private final String envelopeId;
    private final String createdAt;
    private final String modifiedAt;

    /** Constructs from a parsed JSON:API resource object.
     * @param obj resource object
     * @param parentEnvelopeId envelope id
     * @return new instance
     */
    public static Document from(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        return new Document(obj, parentEnvelopeId);
    }

    private Document(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        Map<String, Object> a = obj.attributes();
        this.id         = obj.id();
        this.filename   = str(a.get("filename"));
        this.status     = str(a.get("status"));
        this.metadata   = objectMap(a.get("metadata"));
        this.envelopeId = parentEnvelopeId != null ? parentEnvelopeId : obj.relationshipId("envelope");
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
     * Returns the filename.
     *
     * @return filename
     */
    public String filename() {
        return filename;
    }

    /**
     * Returns the status.
     *
     * @return status
     */
    public String status() {
        return status;
    }

    /** Returns the document metadata map.
     * @return metadata map, may be empty
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns the metadata typed.
     *
     * @return metadata typed
     */
    public Metadata metadataTyped() {
        return Metadata.fromMap(metadata);
    }

    /**
     * Returns the status as enum.
     *
     * @return status as enum
     */
    public DocumentStatus statusAsEnum() {
        return ApiStringEnum.tryParse(DocumentStatus.class, status);
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
     * Returns the created at.
     *
     * @return created at
     */
    public String createdAt() {
        return createdAt;
    }

    /**
     * Returns the modified at.
     *
     * @return modified at
     */
    public String modifiedAt() {
        return modifiedAt;
    }

    /**
     * Returns a string representation.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Document{id='" + id + "', filename='" + filename + "', envelopeId='" + envelopeId + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** Service. */
    public static final class Service {

        private final HttpClient http;

        /**
         * Constructs this service with the given HTTP client.
         *
         * @param http HTTP client
         */
        public Service(HttpClient http) {
            this.http = http;
        }

        /**
         * Lists all documents for the given envelope.
         *
         * @param envelopeId envelope id
         * @return unmodifiable list
         */
        public List<Document> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/documents", Collections.emptyMap());
            List<Document> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Document(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Returns a fluent query builder for filtering and paginating documents.
         *
         * @param envelopeId envelope id to scope the query
         * @return query builder
         */
        public DocumentQuery filter(String envelopeId) {
            return new DocumentQuery(envelopeId, http);
        }

        /**
         * Retrieves a document by id.
         *
         * @param id document id
         * @param envelopeId envelope id
         * @return document
         */
        public Document retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/documents/" + id, Collections.emptyMap());
            return new Document(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        /**
         * Creates a document.
         *
         * @param params creation parameters
         * @return created document
         */
        public Document create(CreateParams params) {
            String body = JsonApiSerializer.dump("documents", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/documents", body);
            return new Document(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        /**
         * Updates a document.
         *
         * @param id document id
         * @param envelopeId envelope id
         * @param params update parameters
         * @return updated document
         */
        public Document update(String id, String envelopeId, UpdateParams params) {
            String body = JsonApiSerializer.dump("documents", id, params.toAttributes(), null);
            String raw  = http.patch("/envelopes/" + envelopeId + "/documents/" + id, body);
            return new Document(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        /**
         * Deletes a document by id.
         *
         * @param id document id
         * @param envelopeId envelope id
         */
        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/documents/" + id, null);
        }

        /**
         * Lists events for a document.
         *
         * @param documentId document id
         * @param envelopeId envelope id
         * @return unmodifiable list
         */
        public List<Event> listEvents(String documentId, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/documents/" + documentId + "/events",
                    Collections.emptyMap());
            List<Event> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(Event.fromResource(obj, null));
            }
            return Collections.unmodifiableList(result);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** CreateParams. */
    public static final class CreateParams {

        private final String envelopeId;
        private final String filename;
        private final String contentBase64;
        private final String contentUrl;
        private final Map<String, Object> metadata;
        private final Map<String, Object> template;
        private final Map<String, Object> duplicate;

        private CreateParams(Builder b) {
            this.envelopeId    = b.envelopeId;
            this.filename      = b.filename;
            this.contentBase64 = b.contentBase64;
            this.contentUrl    = b.contentUrl;
            this.metadata      = b.metadata != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata)) : null;
            this.template      = b.template != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.template)) : null;
            this.duplicate     = b.duplicate != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.duplicate)) : null;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (filename      != null) {
                m.put("filename",       filename);
            }
            if (contentBase64 != null) {
                m.put("content_base64", contentBase64);
            }
            if (contentUrl    != null) {
                m.put("content_url",    contentUrl);
            }
            if (metadata      != null) {
                m.put("metadata",       metadata);
            }
            if (template      != null) {
                m.put("template",       template);
            }
            if (duplicate     != null) {
                m.put("duplicate",      duplicate);
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

        /** Builder. */
        public static final class Builder {
            private String envelopeId;
            private String filename;
            private String contentBase64;
            private String contentUrl;
            private Map<String, Object> metadata;
            private Map<String, Object> template;
            private Map<String, Object> duplicate;

            private Builder() {}

            /**
             * Sets envelope id.
             *
             * @param envelopeId value
             * @return this builder
             */
            public Builder envelopeId(String envelopeId) {
                this.envelopeId = envelopeId;
                return this;
            }

            /**
             * Sets filename.
             *
             * @param filename value
             * @return this builder
             */
            public Builder filename(String filename) {
                this.filename = filename;
                return this;
            }

            /**
             * Sets content base64.
             *
             * @param contentBase64 value
             * @return this builder
             */
            public Builder contentBase64(String contentBase64) {
                this.contentBase64 = contentBase64;
                return this;
            }

            /**
             * Sets content url.
             *
             * @param contentUrl value
             * @return this builder
             */
            public Builder contentUrl(String contentUrl) {
                this.contentUrl = contentUrl;
                return this;
            }

            /**
             * Sets metadata.
             *
             * @param metadata value
             * @return this builder
             */
            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            /**
             * Sets metadata.
             *
             * @param metadata value
             * @return this builder
             */
            public Builder metadata(Metadata metadata) {
                return metadata(metadata != null ? metadata.toMap() : null);
            }

            /**
             * Sets template.
             *
             * @param template value
             * @return this builder
             */
            public Builder template(Map<String, Object> template) {
                this.template = template;
                return this;
            }

            /**
             * Sets template.
             *
             * @param template value
             * @return this builder
             */
            public Builder template(DocumentTemplate template) {
                return template(template != null ? template.toMap() : null);
            }

            /**
             * Sets duplicate.
             *
             * @param duplicate value
             * @return this builder
             */
            public Builder duplicate(Map<String, Object> duplicate) {
                this.duplicate = duplicate;
                return this;
            }

            /**
             * Sets duplicate.
             *
             * @param duplicate value
             * @return this builder
             */
            public Builder duplicate(DocumentDuplicate duplicate) {
                return duplicate(duplicate != null ? duplicate.toMap() : null);
            }

            /**
             * Returns the build.
             *
             * @return build
             */
            public CreateParams build() {
                if (envelopeId == null || envelopeId.isBlank()) {
                    throw new IllegalArgumentException("envelopeId is required");
                }
                if (filename == null || filename.isBlank()) {
                    throw new IllegalArgumentException("filename is required");
                }
                boolean hasContent = (contentBase64 != null && !contentBase64.isBlank())
                    || (contentUrl != null && !contentUrl.isBlank());
                boolean hasTemplate = template != null && !template.isEmpty();
                boolean hasDuplicate = duplicate != null && !duplicate.isEmpty();
                if (!hasContent && !hasTemplate && !hasDuplicate) {
                    throw new IllegalArgumentException(
                        "either contentBase64, contentUrl, template, or duplicate is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** UpdateParams. */
    public static final class UpdateParams {

        private final String filename;
        private final Map<String, Object> metadata;

        private UpdateParams(Builder b) {
            this.filename = b.filename;
            this.metadata = b.metadata != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata)) : null;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (filename != null) {
                m.put("filename", filename);
            }
            if (metadata != null) {
                m.put("metadata", metadata);
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

        /** Builder. */
        public static final class Builder {
            private String filename;
            private Map<String, Object> metadata;

            private Builder() {}

            /**
             * Sets filename.
             *
             * @param filename value
             * @return this builder
             */
            public Builder filename(String filename) {
                this.filename = filename;
                return this;
            }

            /**
             * Sets metadata.
             *
             * @param metadata value
             * @return this builder
             */
            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            /**
             * Sets metadata.
             *
             * @param metadata value
             * @return this builder
             */
            public Builder metadata(Metadata metadata) {
                return metadata(metadata != null ? metadata.toMap() : null);
            }

            /**
             * Returns the build.
             *
             * @return build
             */
            public UpdateParams build() {
                return new UpdateParams(this);
            }
        }
    }
}
