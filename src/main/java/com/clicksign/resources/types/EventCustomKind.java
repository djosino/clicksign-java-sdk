package com.clicksign.resources.types;

/** Kind of custom event for signing evidence. */
public enum EventCustomKind implements ApiStringEnum {
    /** Token delivered via email. */
    TOKEN_EMAIL("token_email"),
    /** Token delivered via SMS. */
    TOKEN_SMS("token_sms");

    private final String apiValue;

    EventCustomKind(String apiValue) {
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
