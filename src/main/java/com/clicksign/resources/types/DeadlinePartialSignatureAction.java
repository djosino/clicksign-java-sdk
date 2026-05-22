package com.clicksign.resources.types;

/** Action to take when the deadline for partial signing is reached. */
public enum DeadlinePartialSignatureAction implements ApiStringEnum {
    /** Closed status. */
    CLOSED("closed"),
    /** Canceled status. */
    CANCELED("canceled");

    private final String apiValue;

    DeadlinePartialSignatureAction(String apiValue) {
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
