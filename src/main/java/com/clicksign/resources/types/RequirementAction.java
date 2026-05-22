package com.clicksign.resources.types;

public enum RequirementAction implements ApiStringEnum {
    AGREE("agree"),
    PROVIDE_EVIDENCE("provide_evidence"),
    RUBRICATE("rubricate"),
    AUTHENTICATE("authenticate");

    private final String apiValue;

    RequirementAction(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
