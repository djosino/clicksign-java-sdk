package com.clicksign.errors;

/** Raised on HTTP 429. Retryable. */
public class RateLimitException extends ClicksignException {

    private final Long retryAfterSeconds;

    public RateLimitException(String message, int statusCode, String requestId,
                              String responseBody, Long retryAfterSeconds) {
        super(message, statusCode, requestId, responseBody);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Seconds from {@code Retry-After} header, if present.
     *
     * @return retry delay in seconds, or {@code null} if header was absent
     */
    public Long retryAfterSeconds() {
        return retryAfterSeconds;
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
