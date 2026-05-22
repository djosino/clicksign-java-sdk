package com.clicksign.resources.types;

public enum AcceptanceTermStatus implements ApiStringEnum {
    ENQUEUED("enqueued"),
    SENT("sent"),
    COMPLETED("completed"),
    CANCELED("canceled"),
    REFUSED("refused"),
    EXPIRED("expired"),
    ERROR("error"),
    PENDING("pending");

    private final String apiValue;

    AcceptanceTermStatus(String apiValue) { this.apiValue = apiValue; }

    @Override
    public String apiValue() { return apiValue; }
}
