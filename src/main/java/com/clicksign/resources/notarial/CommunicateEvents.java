package com.clicksign.resources.notarial;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed configuration for {@code communicate_events} on signers and signature watchers.
 *
 * <p>The API still accepts arbitrary maps; use {@link #toMap()} when calling builders that
 * accept {@code Map<String, Object>} for backward compatibility.
 */
public final class CommunicateEvents {

    private final NotificationChannel signatureRequest;
    private final NotificationChannel signatureReminder;
    private final NotificationChannel documentSigned;

    private CommunicateEvents(Builder b) {
        this.signatureRequest  = b.signatureRequest;
        this.signatureReminder = b.signatureReminder;
        this.documentSigned    = b.documentSigned;
    }

    public NotificationChannel signatureRequest()  { return signatureRequest; }
    public NotificationChannel signatureReminder() { return signatureReminder; }
    public NotificationChannel documentSigned()    { return documentSigned; }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (signatureRequest  != null) m.put("signature_request",  signatureRequest.apiValue());
        if (signatureReminder != null) m.put("signature_reminder", signatureReminder.apiValue());
        if (documentSigned    != null) m.put("document_signed",    documentSigned.apiValue());
        return Collections.unmodifiableMap(m);
    }

    @SuppressWarnings("unchecked")
    public static CommunicateEvents fromMap(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;
        Builder b = builder();
        Object sr = raw.get("signature_request");
        if (sr != null) b.signatureRequest(NotificationChannel.fromApiValue(sr.toString()));
        Object srem = raw.get("signature_reminder");
        if (srem != null) b.signatureReminder(NotificationChannel.fromApiValue(srem.toString()));
        Object ds = raw.get("document_signed");
        if (ds != null) b.documentSigned(NotificationChannel.fromApiValue(ds.toString()));
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private NotificationChannel signatureRequest;
        private NotificationChannel signatureReminder;
        private NotificationChannel documentSigned;

        private Builder() {}

        public Builder signatureRequest(NotificationChannel v)  { this.signatureRequest = v; return this; }
        public Builder signatureReminder(NotificationChannel v) { this.signatureReminder = v; return this; }
        public Builder documentSigned(NotificationChannel v)    { this.documentSigned = v; return this; }

        public CommunicateEvents build() {
            if (signatureRequest == null && signatureReminder == null && documentSigned == null) {
                throw new IllegalArgumentException("at least one channel is required");
            }
            return new CommunicateEvents(this);
        }
    }
}
