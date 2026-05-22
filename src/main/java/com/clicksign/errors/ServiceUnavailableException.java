package com.clicksign.errors;

/** Raised on HTTP 503. Retryable. */
public class ServiceUnavailableException extends ClicksignException {

    private final Long retryAfterSeconds;

    public ServiceUnavailableException(String message, int statusCode, String requestId,
                                       String responseBody, Long retryAfterSeconds) {
        super(message, statusCode, requestId, responseBody);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** Seconds from {@code Retry-After} header, if present. */
    public Long retryAfterSeconds() { return retryAfterSeconds; }

    @Override
    public boolean isRetryable() { return true; }
}
