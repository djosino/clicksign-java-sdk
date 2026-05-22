package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.EnvelopeQuery;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.DeadlinePartialSignatureAction;
import com.clicksign.resources.types.EnvelopeLocale;
import com.clicksign.resources.types.EnvelopeStatus;
import com.clicksign.resources.types.Metadata;

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
    private final Map<String, Object> metadata;
    private final Integer remindInterval;
    private final String deadlineAt;
    private final String deadlinePartialSignatureAction;
    private final String defaultSubject;
    private final String defaultMessage;
    private final String folderId;
    private final String createdAt;
    private final String modifiedAt;

    @SuppressWarnings("unchecked")
    /** Parses a JSON:API resource object (used by {@link com.clicksign.jsonapi.EnvelopeQuery}). */
    public static Envelope from(JsonApiParser.ResourceObject obj) {
        return new Envelope(obj);
    }

    private Envelope(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id                            = obj.id();
        this.name                          = str(a.get("name"));
        this.status                        = str(a.get("status"));
        this.locale                        = str(a.get("locale"));
        this.autoClose                     = bool(a.get("auto_close"));
        this.blockAfterRefusal             = bool(a.get("block_after_refusal"));
        this.metadata                      = objectMap(a.get("metadata"));
        this.remindInterval                = integer(a.get("remind_interval"));
        this.deadlineAt                    = str(a.get("deadline_at"));
        this.deadlinePartialSignatureAction = str(a.get("deadline_partial_signature_action"));
        this.defaultSubject                = str(a.get("default_subject"));
        this.defaultMessage                = str(a.get("default_message"));
        this.folderId                      = obj.relationshipId("folder");
        this.createdAt                     = str(a.get("created"));
        this.modifiedAt                    = str(a.get("modified"));
    }

    public String id()                                { return id; }
    public String name()                              { return name; }
    public String status()                            { return status; }
    public String locale()                            { return locale; }
    public boolean autoClose()                        { return autoClose; }
    public boolean blockAfterRefusal()                { return blockAfterRefusal; }
    public Map<String, Object> metadata()             { return metadata; }

    public Metadata metadataTyped()                   { return Metadata.fromMap(metadata); }
    public EnvelopeStatus statusAsEnum()              { return ApiStringEnum.tryParse(EnvelopeStatus.class, status); }
    public EnvelopeLocale localeAsEnum()            { return ApiStringEnum.tryParse(EnvelopeLocale.class, locale); }
    public DeadlinePartialSignatureAction deadlinePartialSignatureActionAsEnum() {
        return ApiStringEnum.tryParse(DeadlinePartialSignatureAction.class, deadlinePartialSignatureAction);
    }

    public Integer remindInterval()                   { return remindInterval; }
    public String deadlineAt()                        { return deadlineAt; }
    public String deadlinePartialSignatureAction()    { return deadlinePartialSignatureAction; }
    public String defaultSubject()                    { return defaultSubject; }
    public String defaultMessage()                    { return defaultMessage; }
    public String folderId()                          { return folderId; }
    public String createdAt()                         { return createdAt; }
    public String modifiedAt()                        { return modifiedAt; }

    @Override
    public String toString() {
        return "Envelope{id='" + id + "', name='" + name + "', status='" + status + "'}";
    }

    private static String str(Object o)  { return o != null ? o.toString() : null; }
    private static boolean bool(Object o){ return Boolean.TRUE.equals(o) || "true".equals(str(o)); }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) return null;
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    private static Integer integer(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

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

        /** Returns a fluent query builder for filtering, ordering, and paginating envelopes. */
        public EnvelopeQuery filter() {
            return new EnvelopeQuery(http);
        }

        public Envelope retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        public Envelope create(CreateParams params) {
            String body = JsonApiSerializer.dump("envelopes", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post(ENDPOINT, body);
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        public Envelope update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("envelopes", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        public void delete(String id) {
            http.delete(ENDPOINT + "/" + id, null);
        }

        public Envelope activate(String id) {
            String body = JsonApiSerializer.dump("envelopes", id, Collections.emptyMap(), null);
            String raw  = http.post(ENDPOINT + "/" + id + "/activate", body);
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Notifies all signers on the envelope.
         *
         * @see <a href="https://developers.clicksign.com/reference/api-notificar-envelope">Notificar Signatários do Envelope</a>
         */
        public Notification notifyAll(String envelopeId, NotificationParams params) {
            String body = JsonApiSerializer.dump("notifications", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT + "/" + envelopeId + "/notifications", body);
            return Notification.fromResource(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        private final String name;
        private final String locale;
        private final Boolean autoClose;
        private final Boolean blockAfterRefusal;
        private final Integer remindInterval;
        private final Map<String, Object> metadata;
        private final String deadlineAt;
        private final String deadlinePartialSignatureAction;
        private final String defaultSubject;
        private final String defaultMessage;
        private final String folderId;

        private CreateParams(Builder b) {
            this.name                            = b.name;
            this.locale                          = b.locale;
            this.autoClose                       = b.autoClose;
            this.blockAfterRefusal               = b.blockAfterRefusal;
            this.remindInterval                  = b.remindInterval;
            this.metadata                        = b.metadata != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata)) : null;
            this.deadlineAt                      = b.deadlineAt;
            this.deadlinePartialSignatureAction  = b.deadlinePartialSignatureAction;
            this.defaultSubject                  = b.defaultSubject;
            this.defaultMessage                  = b.defaultMessage;
            this.folderId                        = b.folderId;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name                            != null) m.put("name",                              name);
            if (locale                          != null) m.put("locale",                            locale);
            if (autoClose                       != null) m.put("auto_close",                          autoClose);
            if (blockAfterRefusal               != null) m.put("block_after_refusal",                 blockAfterRefusal);
            if (remindInterval                  != null) m.put("remind_interval",                     remindInterval);
            if (metadata                        != null) m.put("metadata",                            metadata);
            if (deadlineAt                      != null) m.put("deadline_at",                         deadlineAt);
            if (deadlinePartialSignatureAction  != null) m.put("deadline_partial_signature_action", deadlinePartialSignatureAction);
            if (defaultSubject                  != null) m.put("default_subject",                     defaultSubject);
            if (defaultMessage                  != null) m.put("default_message",                     defaultMessage);
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
            private Map<String, Object> metadata;
            private String deadlineAt;
            private String deadlinePartialSignatureAction;
            private String defaultSubject;
            private String defaultMessage;
            private String folderId;

            private Builder() {}

            public Builder name(String name)                                    { this.name = name; return this; }
            public Builder locale(String locale)                                { this.locale = locale; return this; }
            public Builder locale(EnvelopeLocale locale)                        { return locale(locale.apiValue()); }
            public Builder autoClose(boolean autoClose)                         { this.autoClose = autoClose; return this; }
            public Builder blockAfterRefusal(boolean v)                         { this.blockAfterRefusal = v; return this; }
            public Builder remindInterval(int days)                             { this.remindInterval = days; return this; }
            public Builder metadata(Map<String, Object> metadata)               { this.metadata = metadata; return this; }
            public Builder metadata(Metadata metadata) {
                return metadata(metadata != null ? metadata.toMap() : null);
            }
            public Builder deadlineAt(String deadlineAt)                        { this.deadlineAt = deadlineAt; return this; }
            public Builder deadlinePartialSignatureAction(String action)        { this.deadlinePartialSignatureAction = action; return this; }
            public Builder deadlinePartialSignatureAction(DeadlinePartialSignatureAction action) {
                return deadlinePartialSignatureAction(action.apiValue());
            }
            public Builder defaultSubject(String subject)                       { this.defaultSubject = subject; return this; }
            public Builder defaultMessage(String message)                       { this.defaultMessage = message; return this; }
            public Builder folderId(String folderId)                            { this.folderId = folderId; return this; }

            public CreateParams build() {
                if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
                EnvelopeValidation.validateOptionalFields(
                    locale, remindInterval, deadlineAt, deadlinePartialSignatureAction, defaultSubject);
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
        private final Integer remindInterval;
        private final Map<String, Object> metadata;
        private final String deadlineAt;
        private final String deadlinePartialSignatureAction;
        private final String defaultSubject;
        private final String defaultMessage;

        private UpdateParams(Builder b) {
            this.name                            = b.name;
            this.status                          = b.status;
            this.locale                          = b.locale;
            this.autoClose                       = b.autoClose;
            this.blockAfterRefusal               = b.blockAfterRefusal;
            this.remindInterval                  = b.remindInterval;
            this.metadata                        = b.metadata != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata)) : null;
            this.deadlineAt                      = b.deadlineAt;
            this.deadlinePartialSignatureAction  = b.deadlinePartialSignatureAction;
            this.defaultSubject                  = b.defaultSubject;
            this.defaultMessage                  = b.defaultMessage;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name                            != null) m.put("name",                              name);
            if (status                          != null) m.put("status",                            status);
            if (locale                          != null) m.put("locale",                            locale);
            if (autoClose                       != null) m.put("auto_close",                          autoClose);
            if (blockAfterRefusal               != null) m.put("block_after_refusal",                 blockAfterRefusal);
            if (remindInterval                  != null) m.put("remind_interval",                     remindInterval);
            if (metadata                        != null) m.put("metadata",                            metadata);
            if (deadlineAt                      != null) m.put("deadline_at",                         deadlineAt);
            if (deadlinePartialSignatureAction  != null) m.put("deadline_partial_signature_action", deadlinePartialSignatureAction);
            if (defaultSubject                  != null) m.put("default_subject",                     defaultSubject);
            if (defaultMessage                  != null) m.put("default_message",                     defaultMessage);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String name;
            private String status;
            private String locale;
            private Boolean autoClose;
            private Boolean blockAfterRefusal;
            private Integer remindInterval;
            private Map<String, Object> metadata;
            private String deadlineAt;
            private String deadlinePartialSignatureAction;
            private String defaultSubject;
            private String defaultMessage;

            private Builder() {}

            public Builder name(String name)                             { this.name = name; return this; }
            public Builder status(String status)                         { this.status = status; return this; }
            public Builder status(EnvelopeStatus status)                 { return status(status.apiValue()); }
            public Builder locale(String locale)                         { this.locale = locale; return this; }
            public Builder locale(EnvelopeLocale locale)                 { return locale(locale.apiValue()); }
            public Builder autoClose(boolean v)                          { this.autoClose = v; return this; }
            public Builder blockAfterRefusal(boolean v)                  { this.blockAfterRefusal = v; return this; }
            public Builder remindInterval(int days)                      { this.remindInterval = days; return this; }
            public Builder metadata(Map<String, Object> metadata)        { this.metadata = metadata; return this; }
            public Builder metadata(Metadata metadata) {
                return metadata(metadata != null ? metadata.toMap() : null);
            }
            public Builder deadlineAt(String deadlineAt)                 { this.deadlineAt = deadlineAt; return this; }
            public Builder deadlinePartialSignatureAction(String action) { this.deadlinePartialSignatureAction = action; return this; }
            public Builder deadlinePartialSignatureAction(DeadlinePartialSignatureAction action) {
                return deadlinePartialSignatureAction(action.apiValue());
            }
            public Builder defaultSubject(String subject)                { this.defaultSubject = subject; return this; }
            public Builder defaultMessage(String message)                { this.defaultMessage = message; return this; }

            public UpdateParams build() {
                EnvelopeValidation.validateOptionalFields(
                    locale, remindInterval, deadlineAt, deadlinePartialSignatureAction, defaultSubject);
                return new UpdateParams(this);
            }
        }
    }
}
