package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.AcceptanceTermWhatsappQuery;
import com.clicksign.resources.types.AcceptanceTermStatus;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.SenderNameOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** WhatsApp acceptance term (Click.Agree). */
public final class AcceptanceTermWhatsapp {

    private final String id;
    private final String title;
    private final String message;
    private final String signerName;
    private final String signerPhone;
    private final String senderPhone;
    private final String senderNameOption;
    private final String senderName;
    private final String status;
    private final String statusFlow;
    private final String sentAt;
    private final String createdAt;
    private final String modifiedAt;

    /**
     * Constructs from a parsed JSON:API resource object.
     *
     * @param obj resource object
     * @return new instance
     */
    public static AcceptanceTermWhatsapp from(JsonApiParser.ResourceObject obj) {
        return new AcceptanceTermWhatsapp(obj);
    }

    private AcceptanceTermWhatsapp(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id                = obj.id();
        this.title             = str(a.get("title"));
        this.message           = str(a.get("message"));
        this.signerName        = str(a.get("signer_name"));
        this.signerPhone       = str(a.get("signer_phone"));
        this.senderPhone       = str(a.get("sender_phone"));
        this.senderNameOption  = str(a.get("sender_name_option"));
        this.senderName        = str(a.get("sender_name"));
        this.status            = str(a.get("status"));
        this.statusFlow        = str(a.get("status_flow"));
        this.sentAt            = str(a.get("sent_at"));
        this.createdAt         = str(a.get("created"));
        this.modifiedAt        = str(a.get("modified"));
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
     * Returns the title.
     *
     * @return title
     */
    public String title() {
        return title;
    }

    /**
     * Returns the message.
     *
     * @return message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the signerName.
     *
     * @return signerName
     */
    public String signerName() {
        return signerName;
    }

    /**
     * Returns the signerPhone.
     *
     * @return signerPhone
     */
    public String signerPhone() {
        return signerPhone;
    }

    /**
     * Returns the senderPhone.
     *
     * @return senderPhone
     */
    public String senderPhone() {
        return senderPhone;
    }

    /**
     * Returns the senderNameOption.
     *
     * @return senderNameOption
     */
    public String senderNameOption() {
        return senderNameOption;
    }

    /**
     * Returns the senderName.
     *
     * @return senderName
     */
    public String senderName() {
        return senderName;
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
     * Returns the status as an {@link AcceptanceTermStatus} enum, or {@code null} if unrecognized.
     *
     * @return AcceptanceTermStatus enum or null
     */
    public AcceptanceTermStatus statusAsEnum() {
        return ApiStringEnum.tryParse(AcceptanceTermStatus.class, status);
    }

    /**
     * Returns the senderNameOption as a {@link SenderNameOption} enum, or {@code null} if unrecognized.
     *
     * @return SenderNameOption enum or null
     */
    public SenderNameOption senderNameOptionAsEnum() {
        return ApiStringEnum.tryParse(SenderNameOption.class, senderNameOption);
    }

    /**
     * Returns the statusFlow.
     *
     * @return statusFlow
     */
    public String statusFlow() {
        return statusFlow;
    }

    /**
     * Returns the sentAt.
     *
     * @return sentAt
     */
    public String sentAt() {
        return sentAt;
    }

    /**
     * Returns the createdAt.
     *
     * @return createdAt
     */
    public String createdAt() {
        return createdAt;
    }

    /**
     * Returns the modifiedAt.
     *
     * @return modifiedAt
     */
    public String modifiedAt() {
        return modifiedAt;
    }

    @Override
    public String toString() {
        return "AcceptanceTermWhatsapp{id='" + id + "', status='" + status + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for AcceptanceTermWhatsapp operations. */
    public static final class Service {

        private static final String ENDPOINT = "/acceptance_term/whatsapps";
        private final HttpClient http;

        /**
         * Constructs service with the given HTTP client.
         *
         * @param http HTTP client
         */
        public Service(HttpClient http) {
            this.http = http;
        }

        /**
         * Lists all AcceptanceTermWhatsapp.
         *
         * @return list of AcceptanceTermWhatsapp
         */
        public List<AcceptanceTermWhatsapp> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<AcceptanceTermWhatsapp> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new AcceptanceTermWhatsapp(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Returns a fluent query builder.
         *
         * @return query builder
         */
        public AcceptanceTermWhatsappQuery filter() {
            return new AcceptanceTermWhatsappQuery(http);
        }

        /**
         * Retrieves AcceptanceTermWhatsapp by id.
         *
         * @param id resource id
         * @return AcceptanceTermWhatsapp
         */
        public AcceptanceTermWhatsapp retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates AcceptanceTermWhatsapp.
         *
         * @param params creation parameters
         * @return created AcceptanceTermWhatsapp
         */
        public AcceptanceTermWhatsapp create(CreateParams params) {
            String body = JsonApiSerializer.dump("acceptance_term_whatsapps", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Cancels a sent acceptance by setting {@code status} to {@code canceled}.
         *
         * @param id acceptance term id
         * @return updated acceptance term
         */
        public AcceptanceTermWhatsapp cancel(String id) {
            String body = JsonApiSerializer.dump("acceptance_term_whatsapps", id,
                Map.of("status", AcceptanceTermStatus.CANCELED.apiValue()), null);
            String raw = http.patch(ENDPOINT + "/" + id, body);
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Updates AcceptanceTermWhatsapp.
         *
         * @param id resource id
         * @param params update parameters
         * @return updated AcceptanceTermWhatsapp
         */
        public AcceptanceTermWhatsapp update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("acceptance_term_whatsapps", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating an acceptance term WhatsApp flow. */
    public static final class CreateParams {

        private final String title;
        private final String senderNameOption;
        private final String senderPhone;
        private final String message;
        private final String signerPhone;
        private final String signerName;

        private CreateParams(Builder b) {
            this.title            = b.title;
            this.senderNameOption = b.senderNameOption;
            this.senderPhone      = b.senderPhone;
            this.message          = b.message;
            this.signerPhone      = b.signerPhone;
            this.signerName       = b.signerName;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title", title);
            m.put("sender_name_option", senderNameOption);
            m.put("message", message);
            m.put("signer_phone", signerPhone);
            m.put("signer_name", signerName);
            if (senderPhone != null) {
                m.put("sender_phone", senderPhone);
            }
            return m;
        }

        /**
         * Returns a new {@link Builder} for CreateParams.
         *
         * @return builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link CreateParams}. */
        public static final class Builder {
            private String title;
            private String senderNameOption;
            private String senderPhone;
            private String message;
            private String signerPhone;
            private String signerName;

            private Builder() {}

            /**
             * Sets title.
             *
             * @param v value
             * @return this
             */
            public Builder title(String v) {
                this.title = v;
                return this;
            }

            /**
             * Sets senderNameOption.
             *
             * @param v value
             * @return this
             */
            public Builder senderNameOption(String v) {
                this.senderNameOption = v;
                return this;
            }

            /**
             * Sets senderNameOption.
             *
             * @param v value
             * @return this
             */
            public Builder senderNameOption(SenderNameOption v) {
                return senderNameOption(v.apiValue());
            }

            /**
             * Sets senderPhone.
             *
             * @param v value
             * @return this
             */
            public Builder senderPhone(String v) {
                this.senderPhone = v;
                return this;
            }

            /**
             * Sets message.
             *
             * @param v value
             * @return this
             */
            public Builder message(String v) {
                this.message = v;
                return this;
            }

            /**
             * Sets signerPhone.
             *
             * @param v value
             * @return this
             */
            public Builder signerPhone(String v) {
                this.signerPhone = v;
                return this;
            }

            /**
             * Sets signerName.
             *
             * @param v value
             * @return this
             */
            public Builder signerName(String v) {
                this.signerName = v;
                return this;
            }

            /**
             * Builds params, validating required fields.
             *
             * @return new params
             * @throws IllegalArgumentException if required field is missing
             */
            public CreateParams build() {
                if (title == null || title.isBlank()) {
                    throw new IllegalArgumentException("title is required");
                }
                if (senderNameOption == null || senderNameOption.isBlank()) {
                    throw new IllegalArgumentException("senderNameOption is required");
                }
                if (message == null || message.isBlank()) {
                    throw new IllegalArgumentException("message is required");
                }
                if (signerPhone == null || signerPhone.isBlank()) {
                    throw new IllegalArgumentException("signerPhone is required");
                }
                if (signerName == null || signerName.isBlank()) {
                    throw new IllegalArgumentException("signerName is required");
                }
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    /** Parameters for updating an AcceptanceTermWhatsapp. */
    public static final class UpdateParams {

        private final String status;

        private UpdateParams(Builder b) {
            this.status = b.status;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (status != null) {
                m.put("status", status);
            }
            return m;
        }

        /**
         * Returns a new {@link Builder} for UpdateParams.
         *
         * @return builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link UpdateParams}. */
        public static final class Builder {
            private String status;

            private Builder() {}

            /**
             * Sets status.
             *
             * @param v value
             * @return this
             */
            public Builder status(String v) {
                this.status = v;
                return this;
            }

            /**
             * Sets status.
             *
             * @param v value
             * @return this
             */
            public Builder status(AcceptanceTermStatus v) {
                return status(v.apiValue());
            }

            /**
             * Builds params, validating required fields.
             *
             * @return new params
             * @throws IllegalArgumentException if required field is missing
             */
            public UpdateParams build() {
                if (status == null || status.isBlank()) {
                    throw new IllegalArgumentException("status is required");
                }
                return new UpdateParams(this);
            }
        }
    }
}
