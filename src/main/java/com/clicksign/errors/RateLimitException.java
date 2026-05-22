package com.clicksign.errors;

/** Raised on HTTP 429. Retryable. */
public class RateLimitException extends ClicksignException {

    public RateLimitException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }

    @Override
    public boolean isRetryable() { return true; }
}
