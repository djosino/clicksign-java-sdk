package com.clicksign.resources.types;

public enum EventCustomKind implements ApiStringEnum {
    TOKEN_EMAIL("token_email"),
    TOKEN_SMS("token_sms");

    private final String apiValue;

    EventCustomKind(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
