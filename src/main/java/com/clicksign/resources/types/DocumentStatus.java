package com.clicksign.resources.types;

public enum DocumentStatus implements ApiStringEnum {
    DRAFT("draft"),
    RUNNING("running"),
    CLOSED("closed"),
    CANCELED("canceled");

    private final String apiValue;

    DocumentStatus(String apiValue) { this.apiValue = apiValue; }

    @Override
    public String apiValue() { return apiValue; }
}
