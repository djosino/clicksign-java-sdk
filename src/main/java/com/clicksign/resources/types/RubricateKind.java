package com.clicksign.resources.types;

/** Kind of rubric/initial for a rubricate requirement. */
public enum RubricateKind implements ApiStringEnum {
    /** Initials rubric. */
    INITIALS("initials"),
    /** Manuscript rubric. */
    MANUSCRIPT("manuscript");

    private final String apiValue;

    RubricateKind(String apiValue) {
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
