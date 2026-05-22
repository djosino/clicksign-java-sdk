package com.clicksign.resources.types;

public enum EnvelopeLocale implements ApiStringEnum {
    PT_BR("pt-BR"),
    EN_US("en-US");

    private final String apiValue;

    EnvelopeLocale(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
