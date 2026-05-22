package com.clicksign.resources.types;

/** Action type for a signing requirement. */
public enum RequirementAction implements ApiStringEnum {
    /** Agree action. */
    AGREE("agree"),
    /** Provide evidence action. */
    PROVIDE_EVIDENCE("provide_evidence"),
    /** Rubricate action. */
    RUBRICATE("rubricate"),
    /** Authenticate action. */
    AUTHENTICATE("authenticate");

    private final String apiValue;

    RequirementAction(String apiValue) {
        this.apiValue = apiValue;
    }

    /**
     * Returns the api value.
     *
     * @return api value
     */
    @Override
    public String apiValue() {
        return apiValue;
    }
}
