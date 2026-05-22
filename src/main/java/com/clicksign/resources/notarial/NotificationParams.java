package com.clicksign.resources.notarial;

import com.clicksign.resources.types.EmailCustomization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Parameters for envelope-wide or per-signer notification requests. */
public final class NotificationParams {

    private final String message;
    private final Map<String, Object> emailCustomization;

    private NotificationParams(Builder b) {
        this.message            = b.message;
        this.emailCustomization = b.emailCustomization != null
            ? Collections.unmodifiableMap(new LinkedHashMap<>(b.emailCustomization)) : null;
    }

    Map<String, Object> toAttributes() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (message            != null) {
            m.put("message",             message);
        }
        if (emailCustomization != null) {
            m.put("email_customization", emailCustomization);
        }
        return m;
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
     * Returns the email customization map.
     *
     * @return email customization map
     */
    public Map<String, Object> emailCustomization() {
        return emailCustomization;
    }

    /**
     * Returns the email customization typed.
     *
     * @return email customization typed
     */
    public EmailCustomization emailCustomizationTyped() {
        return EmailCustomization.fromMap(emailCustomization);
    }

    /**
     * Returns a new builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link NotificationParams}. */
    public static final class Builder {
        private String message;
        private Map<String, Object> emailCustomization;

        private Builder() {}

        /**
         * Sets message.
         *
         * @param message value
         * @return this builder
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets email customization.
         *
         * @param customization value
         * @return this builder
         */
        public Builder emailCustomization(Map<String, Object> customization) {
            this.emailCustomization = customization;
            return this;
        }

        /**
         * Sets email customization.
         *
         * @param customization value
         * @return this builder
         */
        public Builder emailCustomization(EmailCustomization customization) {
            return emailCustomization(customization != null ? customization.toMap() : null);
        }

        /**
         * Builds and validates.
         *
         * @return new instance
         */
        public NotificationParams build() {
            return new NotificationParams(this);
        }
    }
}
