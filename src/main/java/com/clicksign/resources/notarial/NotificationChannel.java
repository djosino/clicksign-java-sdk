package com.clicksign.resources.notarial;

/** Notification channel for signer communicate_events fields. */
public enum NotificationChannel {

    EMAIL("email"),
    SMS("sms"),
    WHATSAPP("whatsapp"),
    NONE("none");

    private final String apiValue;

    NotificationChannel(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

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
