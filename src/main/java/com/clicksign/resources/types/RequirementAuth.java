package com.clicksign.resources.types;

public enum RequirementAuth implements ApiStringEnum {
    EMAIL("email"),
    SMS("sms"),
    WHATSAPP("whatsapp"),
    HANDWRITTEN("handwritten"),
    SELFIE("selfie"),
    OFFICIAL_DOCUMENT("official_document"),
    LIVENESS("liveness"),
    PIX("pix"),
    BIOMETRIC("biometric");

    private final String apiValue;

    RequirementAuth(String apiValue) { this.apiValue = apiValue; }

    @Override
    public String apiValue() { return apiValue; }
}
