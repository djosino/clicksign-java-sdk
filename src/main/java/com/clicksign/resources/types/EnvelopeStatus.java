package com.clicksign.resources.types;

public enum EnvelopeStatus implements ApiStringEnum {
    DRAFT("draft"),
    RUNNING("running"),
    CLOSED("closed"),
    CANCELED("canceled");

    private final String apiValue;

    EnvelopeStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
