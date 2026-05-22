package com.clicksign.resources.types;

/** Status of an acceptance term WhatsApp flow. */
public enum AcceptanceTermStatus implements ApiStringEnum {

    /** Term has been queued for sending. */
    ENQUEUED("enqueued"),

    /** Term has been sent to the signer. */
    SENT("sent"),

    /** Signer has accepted the term. */
    COMPLETED("completed"),

    /** Term was canceled before completion. */
    CANCELED("canceled"),

    /** Signer refused the term. */
    REFUSED("refused"),

    /** Term expired before the signer responded. */
    EXPIRED("expired"),

    /** An error occurred during processing. */
    ERROR("error"),

    /** Term is pending processing. */
    PENDING("pending");

    private final String apiValue;

    AcceptanceTermStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
