package com.clicksign;

public final class ClientConfig {

    private final String apiKey;
    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final int maxRetries;

    private ClientConfig(Builder builder) {
        this.apiKey           = builder.apiKey;
        this.baseUrl          = builder.baseUrl;
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.readTimeoutMs    = builder.readTimeoutMs;
        this.maxRetries       = builder.maxRetries;
    }

    public String apiKey()           { return apiKey; }
    public String baseUrl()          { return baseUrl; }
    public int connectTimeoutMs()    { return connectTimeoutMs; }
    public int readTimeoutMs()       { return readTimeoutMs; }
    public int maxRetries()          { return maxRetries; }

    public static Builder builder()  { return new Builder(); }

    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private int connectTimeoutMs = 2_000;
        private int readTimeoutMs    = 10_000;
        private int maxRetries       = 0;

        private Builder() {}

        public Builder apiKey(String apiKey)               { this.apiKey = apiKey; return this; }
        public Builder baseUrl(String baseUrl)             { this.baseUrl = baseUrl; return this; }
        public Builder connectTimeoutMs(int ms)            { this.connectTimeoutMs = ms; return this; }
        public Builder readTimeoutMs(int ms)               { this.readTimeoutMs = ms; return this; }
        public Builder maxRetries(int maxRetries)          { this.maxRetries = maxRetries; return this; }

        public ClientConfig build()                        { return new ClientConfig(this); }
    }
}
