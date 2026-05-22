package com.clicksign.resources.types;

/** Authentication method for a signing requirement. */
public enum RequirementAuth implements ApiStringEnum {
    /** Email authentication. */
    EMAIL("email"),
    /** SMS authentication. */
    SMS("sms"),
    /** WhatsApp authentication. */
    WHATSAPP("whatsapp"),
    /** Handwritten signature authentication. */
    HANDWRITTEN("handwritten"),
    /** Selfie authentication. */
    SELFIE("selfie"),
    /** Official document authentication. */
    OFFICIAL_DOCUMENT("official_document"),
    /** Liveness check authentication. */
    LIVENESS("liveness"),
    /** PIX authentication. */
    PIX("pix"),
    /** Biometric authentication. */
    BIOMETRIC("biometric");

    private final String apiValue;

    RequirementAuth(String apiValue) {
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
