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

    public String id() {
        return id;
    }

    public String filename() {
        return filename;
    }

    public String status() {
        return status;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public Metadata metadataTyped() {
        return Metadata.fromMap(metadata);
    }

    public DocumentStatus statusAsEnum() {
        return ApiStringEnum.tryParse(DocumentStatus.class, status);
    }

    public String envelopeId() {
        return envelopeId;
    }

    public String createdAt() {
        return createdAt;
    }

    public String modifiedAt() {
        return modifiedAt;
    }

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

    public static final class Service {

        private final HttpClient http;

        public Service(HttpClient http) {
            this.http = http;
        }

        public List<Document> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/documents", Collections.emptyMap());
            List<Document> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Document(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        /** Returns a fluent query builder for filtering and paginating documents. */
        public DocumentQuery filter(String envelopeId) {
            return new DocumentQuery(envelopeId, http);
        }

        public Document retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/documents/" + id, Collections.emptyMap());
            return new Document(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        public Document create(CreateParams params) {
            String body = JsonApiSerializer.dump("documents", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/documents", body);
            return new Document(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        public Document update(String id, String envelopeId, UpdateParams params) {
            String body = JsonApiSerializer.dump("documents", id, params.toAttributes(), null);
            String raw  = http.patch("/envelopes/" + envelopeId + "/documents/" + id, body);
            return new Document(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/documents/" + id, null);
        }

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

    public static final class CreateParams {

        final String envelopeId;
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

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String envelopeId;
            private String filename;
            private String contentBase64;
            private String contentUrl;
            private Map<String, Object> metadata;
            private Map<String, Object> template;
            private Map<String, Object> duplicate;

            private Builder() {}

            public Builder envelopeId(String envelopeId) {
                this.envelopeId = envelopeId;
                return this;
            }

            public Builder filename(String filename) {
                this.filename = filename;
                return this;
            }

            public Builder contentBase64(String contentBase64) {
                this.contentBase64 = contentBase64;
                return this;
            }

            public Builder contentUrl(String contentUrl) {
                this.contentUrl = contentUrl;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public Builder metadata(Metadata metadata) {
                return metadata(metadata != null ? metadata.toMap() : null);
            }

            public Builder template(Map<String, Object> template) {
                this.template = template;
                return this;
            }

            public Builder template(DocumentTemplate template) {
                return template(template != null ? template.toMap() : null);
            }

            public Builder duplicate(Map<String, Object> duplicate) {
                this.duplicate = duplicate;
                return this;
            }

            public Builder duplicate(DocumentDuplicate duplicate) {
                return duplicate(duplicate != null ? duplicate.toMap() : null);
            }

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

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String filename;
            private Map<String, Object> metadata;

            private Builder() {}

            public Builder filename(String filename) {
                this.filename = filename;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public Builder metadata(Metadata metadata) {
                return metadata(metadata != null ? metadata.toMap() : null);
            }

            public UpdateParams build() {
                return new UpdateParams(this);
            }
        }
    }
}
