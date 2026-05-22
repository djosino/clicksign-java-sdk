package com.clicksign.resources.types;

/** Locale of an envelope. */
public enum EnvelopeLocale implements ApiStringEnum {
    /** Portuguese (Brazil) locale. */
    PT_BR("pt-BR"),
    /** English (US) locale. */
    EN_US("en-US");

    private final String apiValue;

    EnvelopeLocale(String apiValue) {
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
