package com.clicksign.errors;

/**
 * Base exception for all Clicksign SDK errors.
 *
 * <p>HTTP errors expose {@link #statusCode()}, {@link #requestId()} and {@link #responseBody()}.
 */
public class ClicksignException extends RuntimeException {

    private final int statusCode;
    private final String requestId;
    private final String responseBody;

    public ClicksignException(String message, int statusCode, String requestId, String responseBody) {
        super(message);
        this.statusCode   = statusCode;
        this.requestId    = requestId;
        this.responseBody = responseBody;
    }

    public ClicksignException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode   = 0;
        this.requestId    = null;
        this.responseBody = null;
    }

    public int statusCode() {
        return statusCode;
    }

    public String requestId() {
        return requestId;
    }

    public String responseBody() {
        return responseBody;
    }

    /**
     * Whether this error is safe to retry.
     *
     * @return {@code true} if the request can be retried
     */
    public boolean isRetryable() {
        return false;
    }
}
