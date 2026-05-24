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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Constructs from a JSON:API resource object.
     *
     * @param obj resource object
     * @return new instance
     */
    @SuppressWarnings("unchecked")
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
     * Returns the status.
     *
     * @return status
     */
    public String status() {
        return status;
    }

    /**
     * Returns the locale.
     *
     * @return locale
     */
    public String locale() {
        return locale;
    }

    /**
     * Returns whether the envelope auto-closes.
     *
     * @return auto-close flag
     */
    public boolean autoClose() {
        return autoClose;
    }

    /**
     * Returns whether the envelope blocks after refusal.
     *
     * @return block-after-refusal flag
     */
    public boolean blockAfterRefusal() {
        return blockAfterRefusal;
    }

    /**
     * Returns the metadata map.
     *
     * @return metadata map
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
     * Returns the status as an enum.
     *
     * @return status enum
     */
    public EnvelopeStatus statusAsEnum() {
        return ApiStringEnum.tryParse(EnvelopeStatus.class, status);
    }

    /**
     * Returns the locale as an enum.
     *
     * @return locale enum
     */
    public EnvelopeLocale localeAsEnum() {
        return ApiStringEnum.tryParse(EnvelopeLocale.class, locale);
    }

    /**
     * Returns the deadline partial signature action as an enum.
     *
     * @return action enum
     */
    public DeadlinePartialSignatureAction deadlinePartialSignatureActionAsEnum() {
        return ApiStringEnum.tryParse(DeadlinePartialSignatureAction.class, deadlinePartialSignatureAction);
    }

    /**
     * Returns the remind interval.
     *
     * @return remind interval
     */
    public Integer remindInterval() {
        return remindInterval;
    }

    /**
     * Returns the deadline at.
     *
     * @return deadline at
     */
    public String deadlineAt() {
        return deadlineAt;
    }

    /**
     * Returns the deadline partial signature action.
     *
     * @return deadline partial signature action
     */
    public String deadlinePartialSignatureAction() {
        return deadlinePartialSignatureAction;
    }

    /**
     * Returns the default subject.
     *
     * @return default subject
     */
    public String defaultSubject() {
        return defaultSubject;
    }

    /**
     * Returns the default message.
     *
     * @return default message
     */
    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * Returns the folder id.
     *
     * @return folder id
     */
    public String folderId() {
        return folderId;
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

    @Override
    public String toString() {
        return "Envelope{id='" + id + "', name='" + name + "', status='" + status + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static boolean bool(Object o) {
        return Boolean.TRUE.equals(o) || "true".equals(str(o));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    private static Integer integer(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Envelope operations. */
    public static final class Service {

        private static final String ENDPOINT = "/envelopes";
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
        public List<Envelope> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<Envelope> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Envelope(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Returns a fluent query builder for filtering, ordering, and paginating envelopes.
         *
         * @return query builder
         */
        public EnvelopeQuery filter() {
            return new EnvelopeQuery(http);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id resource id
         * @return resource
         */
        public Envelope retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public Envelope create(CreateParams params) {
            String body = JsonApiSerializer.dump("envelopes", null, params.toAttributes(), params.toRelationships());
            String raw  = http.post(ENDPOINT, body);
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Updates resource.
         *
         * @param id resource id
         * @param params update parameters
         * @return updated resource
         */
        public Envelope update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("envelopes", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new Envelope(JsonApiParser.parse(raw).firstData());
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
         * Activates an envelope.
         *
         * @param id envelope id
         * @return updated envelope
         */
        public Envelope activate(String id) {
            String body = JsonApiSerializer.dump("envelopes", id, Collections.emptyMap(), null);
            String raw  = http.post(ENDPOINT + "/" + id + "/activate", body);
            return new Envelope(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Notifies all signers on the envelope.
         *
         * @param envelopeId envelope id
         * @param params notification parameters
         * @return notification resource
         * @see <a href="https://developers.clicksign.com/reference/api-notificar-envelope">Notificar Signatários do Envelope</a>
         */
        public Notification notifyAll(String envelopeId, NotificationParams params) {
            String body = JsonApiSerializer.dump("notifications", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT + "/" + envelopeId + "/notifications", body);
            return Notification.fromResource(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating an envelope. */
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
            if (name                            != null) {
                m.put("name",                              name);
            }
            if (locale                          != null) {
                m.put("locale",                            locale);
            }
            if (autoClose                       != null) {
                m.put("auto_close",                          autoClose);
            }
            if (blockAfterRefusal               != null) {
                m.put("block_after_refusal",                 blockAfterRefusal);
            }
            if (remindInterval                  != null) {
                m.put("remind_interval",                     remindInterval);
            }
            if (metadata                        != null) {
                m.put("metadata",                            metadata);
            }
            if (deadlineAt                      != null) {
                m.put("deadline_at",                         deadlineAt);
            }
            if (deadlinePartialSignatureAction  != null) {
                m.put("deadline_partial_signature_action", deadlinePartialSignatureAction);
            }
            if (defaultSubject                  != null) {
                m.put("default_subject",                     defaultSubject);
            }
            if (defaultMessage                  != null) {
                m.put("default_message",                     defaultMessage);
            }
            return m;
        }

        Map<String, Object> toRelationships() {
            if (folderId == null) {
                return Collections.emptyMap();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "folders");
            data.put("id",   folderId);
            Map<String, Object> rel = new LinkedHashMap<>();
            rel.put("data", data);
            Map<String, Object> rels = new LinkedHashMap<>();
            rels.put("folder", rel);
            return rels;
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

            /**
             * Sets name.
             *
             * @param name value
             * @return this builder
             */
            public Builder name(String name) {
                this.name = name;
                return this;
            }

            /**
             * Sets locale.
             *
             * @param locale value
             * @return this builder
             */
            public Builder locale(String locale) {
                this.locale = locale;
                return this;
            }

            /**
             * Sets locale.
             *
             * @param locale value
             * @return this builder
             */
            public Builder locale(EnvelopeLocale locale) {
                return locale(locale != null ? locale.apiValue() : null);
            }

            /**
             * Sets auto-close flag.
             *
             * @param autoClose value
             * @return this builder
             */
            public Builder autoClose(boolean autoClose) {
                this.autoClose = autoClose;
                return this;
            }

            /**
             * Sets block-after-refusal flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder blockAfterRefusal(boolean v) {
                this.blockAfterRefusal = v;
                return this;
            }

            /**
             * Sets remind interval in days.
             *
             * @param days value
             * @return this builder
             */
            public Builder remindInterval(int days) {
                this.remindInterval = days;
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
             * Sets deadline at.
             *
             * @param deadlineAt value
             * @return this builder
             */
            public Builder deadlineAt(String deadlineAt) {
                this.deadlineAt = deadlineAt;
                return this;
            }

            /**
             * Sets deadline partial signature action.
             *
             * @param action value
             * @return this builder
             */
            public Builder deadlinePartialSignatureAction(String action) {
                this.deadlinePartialSignatureAction = action;
                return this;
            }

            /**
             * Sets deadline partial signature action.
             *
             * @param action value
             * @return this builder
             */
            public Builder deadlinePartialSignatureAction(DeadlinePartialSignatureAction action) {
                return deadlinePartialSignatureAction(action != null ? action.apiValue() : null);
            }

            /**
             * Sets default subject.
             *
             * @param subject value
             * @return this builder
             */
            public Builder defaultSubject(String subject) {
                this.defaultSubject = subject;
                return this;
            }

            /**
             * Sets default message.
             *
             * @param message value
             * @return this builder
             */
            public Builder defaultMessage(String message) {
                this.defaultMessage = message;
                return this;
            }

            /**
             * Sets folder id.
             *
             * @param folderId value
             * @return this builder
             */
            public Builder folderId(String folderId) {
                this.folderId = folderId;
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
                EnvelopeValidation.validateOptionalFields(
                    locale, remindInterval, deadlineAt, deadlinePartialSignatureAction, defaultSubject);
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating an envelope. */
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
            if (name                            != null) {
                m.put("name",                              name);
            }
            if (status                          != null) {
                m.put("status",                            status);
            }
            if (locale                          != null) {
                m.put("locale",                            locale);
            }
            if (autoClose                       != null) {
                m.put("auto_close",                          autoClose);
            }
            if (blockAfterRefusal               != null) {
                m.put("block_after_refusal",                 blockAfterRefusal);
            }
            if (remindInterval                  != null) {
                m.put("remind_interval",                     remindInterval);
            }
            if (metadata                        != null) {
                m.put("metadata",                            metadata);
            }
            if (deadlineAt                      != null) {
                m.put("deadline_at",                         deadlineAt);
            }
            if (deadlinePartialSignatureAction  != null) {
                m.put("deadline_partial_signature_action", deadlinePartialSignatureAction);
            }
            if (defaultSubject                  != null) {
                m.put("default_subject",                     defaultSubject);
            }
            if (defaultMessage                  != null) {
                m.put("default_message",                     defaultMessage);
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

            /**
             * Sets name.
             *
             * @param name value
             * @return this builder
             */
            public Builder name(String name) {
                this.name = name;
                return this;
            }

            /**
             * Sets status.
             *
             * @param status value
             * @return this builder
             */
            public Builder status(String status) {
                this.status = status;
                return this;
            }

            /**
             * Sets status.
             *
             * @param status value
             * @return this builder
             */
            public Builder status(EnvelopeStatus status) {
                return status(status != null ? status.apiValue() : null);
            }

            /**
             * Sets locale.
             *
             * @param locale value
             * @return this builder
             */
            public Builder locale(String locale) {
                this.locale = locale;
                return this;
            }

            /**
             * Sets locale.
             *
             * @param locale value
             * @return this builder
             */
            public Builder locale(EnvelopeLocale locale) {
                return locale(locale != null ? locale.apiValue() : null);
            }

            /**
             * Sets auto-close flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder autoClose(boolean v) {
                this.autoClose = v;
                return this;
            }

            /**
             * Sets block-after-refusal flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder blockAfterRefusal(boolean v) {
                this.blockAfterRefusal = v;
                return this;
            }

            /**
             * Sets remind interval in days.
             *
             * @param days value
             * @return this builder
             */
            public Builder remindInterval(int days) {
                this.remindInterval = days;
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
             * Sets deadline at.
             *
             * @param deadlineAt value
             * @return this builder
             */
            public Builder deadlineAt(String deadlineAt) {
                this.deadlineAt = deadlineAt;
                return this;
            }

            /**
             * Sets deadline partial signature action.
             *
             * @param action value
             * @return this builder
             */
            public Builder deadlinePartialSignatureAction(String action) {
                this.deadlinePartialSignatureAction = action;
                return this;
            }

            /**
             * Sets deadline partial signature action.
             *
             * @param action value
             * @return this builder
             */
            public Builder deadlinePartialSignatureAction(DeadlinePartialSignatureAction action) {
                return deadlinePartialSignatureAction(action != null ? action.apiValue() : null);
            }

            /**
             * Sets default subject.
             *
             * @param subject value
             * @return this builder
             */
            public Builder defaultSubject(String subject) {
                this.defaultSubject = subject;
                return this;
            }

            /**
             * Sets default message.
             *
             * @param message value
             * @return this builder
             */
            public Builder defaultMessage(String message) {
                this.defaultMessage = message;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public UpdateParams build() {
                EnvelopeValidation.validateOptionalFields(
                    locale, remindInterval, deadlineAt, deadlinePartialSignatureAction, defaultSubject);
                return new UpdateParams(this);
            }
        }
    }
}
