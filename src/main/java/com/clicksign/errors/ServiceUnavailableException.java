package com.clicksign.errors;

/** Raised on HTTP 503. Retryable. */
public class ServiceUnavailableException extends ClicksignException {

    /** Seconds to wait before retrying, or {@code null} if absent. */
    private final Long retryAfterSeconds;

    /**
     * Constructs a new 503 Service Unavailable exception.
     *
     * @param message            detail message
     * @param statusCode         HTTP status code
     * @param requestId          API request id
     * @param responseBody       raw response body
     * @param retryAfterSeconds  retry-after delay in seconds, or {@code null}
     */
    public ServiceUnavailableException(String message, int statusCode, String requestId,
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
