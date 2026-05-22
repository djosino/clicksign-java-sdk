package com.clicksign;

/** API environment selection. */
public enum Environment {
    /** Production environment. */
    PRODUCTION("https://app.clicksign.com/api/v3"),
    /** Sandbox environment. */
    SANDBOX("https://sandbox.clicksign.com/api/v3");

    private final String baseUrl;

    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the base URL for this environment.
     *
     * @return base URL
     */
    public String baseUrl() {
        return baseUrl;
    }
}
