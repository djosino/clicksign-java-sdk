package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.ResourceQuery;
import com.clicksign.resources.types.AcceptanceTermStatus;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.SenderNameOption;

import java.util.*;

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

    public String id()               { return id; }
    public String title()            { return title; }
    public String message()          { return message; }
    public String signerName()       { return signerName; }
    public String signerPhone()      { return signerPhone; }
    public String senderPhone()      { return senderPhone; }
    public String senderNameOption() { return senderNameOption; }
    public String senderName()       { return senderName; }
    public String status()           { return status; }
    public AcceptanceTermStatus statusAsEnum() {
        return ApiStringEnum.tryParse(AcceptanceTermStatus.class, status);
    }
    public SenderNameOption senderNameOptionAsEnum() {
        return ApiStringEnum.tryParse(SenderNameOption.class, senderNameOption);
    }
    public String statusFlow()       { return statusFlow; }
    public String sentAt()           { return sentAt; }
    public String createdAt()        { return createdAt; }
    public String modifiedAt()       { return modifiedAt; }

    @Override
    public String toString() {
        return "AcceptanceTermWhatsapp{id='" + id + "', status='" + status + "'}";
    }

    private static String str(Object o) { return o != null ? o.toString() : null; }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/acceptance_term/whatsapps";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public List<AcceptanceTermWhatsapp> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<AcceptanceTermWhatsapp> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new AcceptanceTermWhatsapp(obj));
            }
            return Collections.unmodifiableList(result);
        }

        public ResourceQuery<AcceptanceTermWhatsapp> filter() {
            return new ResourceQuery<>(ENDPOINT, http, AcceptanceTermWhatsapp::new);
        }

        public AcceptanceTermWhatsapp retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }

        public AcceptanceTermWhatsapp create(CreateParams params) {
            String body = JsonApiSerializer.dump("acceptance_term_whatsapps", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }

        /** Cancels a sent acceptance by setting {@code status} to {@code canceled}. */
        public AcceptanceTermWhatsapp cancel(String id) {
            String body = JsonApiSerializer.dump("acceptance_term_whatsapps", id,
                Map.of("status", AcceptanceTermStatus.CANCELED.apiValue()), null);
            String raw = http.patch(ENDPOINT + "/" + id, body);
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }

        public AcceptanceTermWhatsapp update(String id, UpdateParams params) {
            String body = JsonApiSerializer.dump("acceptance_term_whatsapps", id, params.toAttributes(), null);
            String raw  = http.patch(ENDPOINT + "/" + id, body);
            return new AcceptanceTermWhatsapp(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

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
            if (senderPhone != null) m.put("sender_phone", senderPhone);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String title;
            private String senderNameOption;
            private String senderPhone;
            private String message;
            private String signerPhone;
            private String signerName;

            private Builder() {}

            public Builder title(String v)            { this.title = v; return this; }
            public Builder senderNameOption(String v) { this.senderNameOption = v; return this; }
            public Builder senderNameOption(SenderNameOption v) { return senderNameOption(v.apiValue()); }
            public Builder senderPhone(String v)      { this.senderPhone = v; return this; }
            public Builder message(String v)          { this.message = v; return this; }
            public Builder signerPhone(String v)      { this.signerPhone = v; return this; }
            public Builder signerName(String v)       { this.signerName = v; return this; }

            public CreateParams build() {
                if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
                if (senderNameOption == null || senderNameOption.isBlank()) {
                    throw new IllegalArgumentException("senderNameOption is required");
                }
                if (message == null || message.isBlank()) throw new IllegalArgumentException("message is required");
                if (signerPhone == null || signerPhone.isBlank()) {
                    throw new IllegalArgumentException("signerPhone is required");
                }
                if (signerName == null || signerName.isBlank()) throw new IllegalArgumentException("signerName is required");
                return new CreateParams(this);
            }
        }
    }

    // ── UpdateParams ─────────────────────────────────────────────────────────

    public static final class UpdateParams {

        private final String status;

        private UpdateParams(Builder b) { this.status = b.status; }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (status != null) m.put("status", status);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String status;

            private Builder() {}

            public Builder status(String v) { this.status = v; return this; }
            public Builder status(AcceptanceTermStatus v) { return status(v.apiValue()); }

            public UpdateParams build() {
                if (status == null || status.isBlank()) throw new IllegalArgumentException("status is required");
                return new UpdateParams(this);
            }
        }
    }
}
