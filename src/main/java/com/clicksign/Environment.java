package com.clicksign;

public enum Environment {
    PRODUCTION("https://app.clicksign.com/api/v3"),
    SANDBOX("https://sandbox.clicksign.com/api/v3");

    private final String baseUrl;

    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String baseUrl() {
        return baseUrl;
    }
}
