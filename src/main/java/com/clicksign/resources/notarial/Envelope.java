package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.*;

/**
 * Represents a Clicksign envelope.
 *
 * <pre>{@code
 * Envelope envelope = client.envelopes().create(
 *     Envelope.CreateParams.builder()
 *         .name("Contrato de Prestação de Serviços")
 *         .locale("pt-BR")
 *         .autoClose(true)
 *         .build()
 * );
 * System.out.println(envelope.id());
 * }</pre>
 */
public final class Envelope {

    private final String id;
    private final String name;
    private final String status;
    private final String locale;
    private final boolean autoClose;
    private final boolean blockAfterRefusal;
    private final String metadata;
    private final String defaultSubject;
    private final String defaultMessage;
    private final String folderId;
    private final String createdAt;
    private final String modifiedAt;

    private Envelope(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id                = obj.id();
        this.name              = str(a.get("name"));
        this.status            = str(a.get("status"));
        this.locale            = str(a.get("locale"));
        this.autoClose         = bool(a.get("auto_close"));
        this.blockAfterRefusal = bool(a.get("block_after_refusal"));
        this.metadata          = str(a.get("metadata"));
        this.defaultSubject    = str(a.get("default_subject"));
        this.defaultMessage    = str(a.get("default_message"));
        this.folderId          = obj.relationshipId("folder");
        this.createdAt         = str(a.get("created"));
        this.modifiedAt        = str(a.get("modified"));
    }

    public String id()                { return id; }
    public String name()              { return name; }
    public String status()            { return status; }
    public String locale()            { return locale; }
    public boolean autoClose()        { return autoClose; }
    public boolean blockAfterRefusal(){ return blockAfterRefusal; }
    public String metadata()          { return metadata; }
    public String defaultSubject()    { return defaultSubject; }
    public String defaultMessage()    { return defaultMessage; }
    public String folderId()          { return folderId; }
    public String createdAt()         { return createdAt; }
    public String modifiedAt()        { return modifiedAt; }

    @Override
    public String toString() {
        return "Envelope{id='" + id + "', name='" + name + "', status='" + status + "'}";
    }

    private static String str(Object o)  { return o != null ? o.toString() : null; }
    private static boolean bool(Object o){ return Boolean.TRUE.equals(o) || "true".equals(str(o)); }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/envelopes";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<Envelope> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Envelope> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Envelope(obj));
            }
            return Collections.unmodifiableList(result);
        }

        public Envelope retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Envelope(JsonApiParser.parse(raw).data().get(0));
        }

        public Envelope create(CreateParams params) {
            String body = JsonApiSerializer.dump("envelopes", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post(ENDPOINT, body);
            return new Envelope(JsonApiParser.parse(raw).data().get(0));
        }

        public Envelope update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("envelopes", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Envelope(JsonApiParser.parse(raw).data().get(0));
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }

        public Envelope activate(String id) {
            String raw = http.post(ENDPOINT + "/" + id + "/activate", "{}");
            return new Envelope(JsonApiParser.parse(raw).data().get(0));
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        private final String name;
        private final String locale;
        private final Boolean autoClose;
        private final Boolean blockAfterRefusal;
        private final Integer remindInterval;
        private final String metadata;
        private final String defaultSubject;
        private final String defaultMessage;
        private final String folderId;

        private CreateParams(Builder b) {
            this.name              = b.name;
            this.locale            = b.locale;
            this.autoClose         = b.autoClose;
            this.blockAfterRefusal = b.blockAfterRefusal;
            this.remindInterval    = b.remindInterval;
            this.metadata          = b.metadata;
            this.defaultSubject    = b.defaultSubject;
            this.defaultMessage    = b.defaultMessage;
            this.folderId          = b.folderId;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name              != null) m.put("name",               name);
            if (locale            != null) m.put("locale",             locale);
            if (autoClose         != null) m.put("auto_close",         autoClose);
            if (blockAfterRefusal != null) m.put("block_after_refusal",blockAfterRefusal);
            if (remindInterval    != null) m.put("remind_interval",    remindInterval);
            if (metadata          != null) m.put("metadata",           metadata);
            if (defaultSubject    != null) m.put("default_subject",    defaultSubject);
            if (defaultMessage    != null) m.put("default_message",    defaultMessage);
            return m;
        }

        Map<String, Object> toRelationships() {
            if (folderId == null) return Collections.emptyMap();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "folders");
            data.put("id",   folderId);
            Map<String, Object> rel = new LinkedHashMap<>();
            rel.put("data", data);
            Map<String, Object> rels = new LinkedHashMap<>();
            rels.put("folder", rel);
            return rels;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;
            private String locale;
            private Boolean autoClose;
            private Boolean blockAfterRefusal;
            private Integer remindInterval;
            private String metadata;
            private String defaultSubject;
            private String defaultMessage;
            private String folderId;

            private Builder() {}

            public Builder name(String name)                        { this.name = name; return this; }
            public Builder locale(String locale)                    { this.locale = locale; return this; }
            public Builder autoClose(boolean autoClose)             { this.autoClose = autoClose; return this; }
            public Builder blockAfterRefusal(boolean v)             { this.blockAfterRefusal = v; return this; }
            public Builder remindInterval(int days)                 { this.remindInterval = days; return this; }
            public Builder metadata(String metadata)                { this.metadata = metadata; return this; }
            public Builder defaultSubject(String subject)           { this.defaultSubject = subject; return this; }
            public Builder defaultMessage(String message)           { this.defaultMessage = message; return this; }
            public Builder folderId(String folderId)               { this.folderId = folderId; return this; }

            public CreateParams build() {
                if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    public static final class UpdateParams {

        private final String name;
        private final String status;
        private final String locale;
        private final Boolean autoClose;
        private final Boolean blockAfterRefusal;

        private UpdateParams(Builder b) {
            this.name              = b.name;
            this.status            = b.status;
            this.locale            = b.locale;
            this.autoClose         = b.autoClose;
            this.blockAfterRefusal = b.blockAfterRefusal;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name              != null) m.put("name",               name);
            if (status            != null) m.put("status",             status);
            if (locale            != null) m.put("locale",             locale);
            if (autoClose         != null) m.put("auto_close",         autoClose);
            if (blockAfterRefusal != null) m.put("block_after_refusal",blockAfterRefusal);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;
            private String status;
            private String locale;
            private Boolean autoClose;
            private Boolean blockAfterRefusal;

            private Builder() {}

            public Builder name(String name)            { this.name = name; return this; }
            public Builder status(String status)        { this.status = status; return this; }
            public Builder locale(String locale)        { this.locale = locale; return this; }
            public Builder autoClose(boolean v)         { this.autoClose = v; return this; }
            public Builder blockAfterRefusal(boolean v) { this.blockAfterRefusal = v; return this; }

            public UpdateParams build() { return new UpdateParams(this); }
        }
    }
}
