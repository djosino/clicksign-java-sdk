package com.clicksign.resources.types;

public enum RubricateKind implements ApiStringEnum {
    INITIALS("initials"),
    MANUSCRIPT("manuscript");

    private final String apiValue;

    RubricateKind(String apiValue) { this.apiValue = apiValue; }

    @Override
    public String apiValue() { return apiValue; }
}
