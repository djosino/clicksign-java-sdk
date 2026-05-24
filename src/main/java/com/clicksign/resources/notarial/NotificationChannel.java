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
     * <p>Returns {@code null} when {@code value} is {@code null} (field absent in JSON).
     * Throws for non-null values that match no known channel.
     *
     * @param value api value string, or {@code null}
     * @return matching channel, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null and matches no channel
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
