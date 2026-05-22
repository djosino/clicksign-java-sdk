package com.clicksign.resources.notarial;

/** Notification channel for signer communicate_events fields. */
public enum NotificationChannel {

    /** Email notification channel. */
    EMAIL("email"),
    /** SMS notification channel. */
    SMS("sms"),
    /** WhatsApp notification channel. */
    WHATSAPP("whatsapp"),
    /** No notification. */
    NONE("none");

    private final String apiValue;

    NotificationChannel(String apiValue) {
        this.apiValue = apiValue;
    }

    /**
     * Returns the api value.
     *
     * @return api value
     */
    public String apiValue() {
        return apiValue;
    }

    /**
     * Returns the channel matching the given api value.
     *
     * @param value api value string
     * @return matching channel
     * @throws IllegalArgumentException if no channel matches
     */
    public static NotificationChannel fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (NotificationChannel ch : values()) {
            if (ch.apiValue.equals(value)) {
                return ch;
            }
        }
        throw new IllegalArgumentException("unknown notification channel: " + value);
    }
}
