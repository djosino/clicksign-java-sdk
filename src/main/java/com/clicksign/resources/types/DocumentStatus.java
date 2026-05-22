package com.clicksign.resources.types;

/** Status of a document within an envelope. */
public enum DocumentStatus implements ApiStringEnum {
    /** Draft status. */
    DRAFT("draft"),
    /** Running status. */
    RUNNING("running"),
    /** Closed status. */
    CLOSED("closed"),
    /** Canceled status. */
    CANCELED("canceled");

    private final String apiValue;

    DocumentStatus(String apiValue) {
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
