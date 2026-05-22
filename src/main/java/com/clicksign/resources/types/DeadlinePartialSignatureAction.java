package com.clicksign.resources.types;

public enum DeadlinePartialSignatureAction implements ApiStringEnum {
    CLOSED("closed"),
    CANCELED("canceled");

    private final String apiValue;

    DeadlinePartialSignatureAction(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
