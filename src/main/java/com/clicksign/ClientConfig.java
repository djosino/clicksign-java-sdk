package com.clicksign;

/** ClientConfig. */
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

    /**
     * Returns the api key.
     *
     * @return api key
     */
    public String apiKey() {
        return apiKey;
    }

    /**
     * Returns the base url.
     *
     * @return base url
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * Returns the connect timeout ms.
     *
     * @return connect timeout ms
     */
    public int connectTimeoutMs() {
        return connectTimeoutMs;
    }

    /**
     * Returns the read timeout ms.
     *
     * @return read timeout ms
     */
    public int readTimeoutMs() {
        return readTimeoutMs;
    }

    /**
     * Returns the max retries.
     *
     * @return max retries
     */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Returns a new builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder. */
    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private int connectTimeoutMs = 2_000;
        private int readTimeoutMs    = 10_000;
        private int maxRetries       = 0;

        private Builder() {}

        /**
         * Sets api key.
         *
         * @param apiKey value
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets base url.
         *
         * @param baseUrl value
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets connect timeout ms.
         *
         * @param ms value
         * @return this builder
         */
        public Builder connectTimeoutMs(int ms) {
            this.connectTimeoutMs = ms;
            return this;
        }

        /**
         * Sets read timeout ms.
         *
         * @param ms value
         * @return this builder
         */
        public Builder readTimeoutMs(int ms) {
            this.readTimeoutMs = ms;
            return this;
        }

        /**
         * Sets max retries.
         *
         * @param maxRetries value
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Returns the build.
         *
         * @return build
         */
        public ClientConfig build() {
            return new ClientConfig(this);
        }
    }
}
