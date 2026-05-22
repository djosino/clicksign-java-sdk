package com.clicksign.resources.types;

/** Status of an envelope. */
public enum EnvelopeStatus implements ApiStringEnum {
    /** Draft status. */
    DRAFT("draft"),
    /** Running status. */
    RUNNING("running"),
    /** Closed status. */
    CLOSED("closed"),
    /** Canceled status. */
    CANCELED("canceled");

    private final String apiValue;

    EnvelopeStatus(String apiValue) {
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
