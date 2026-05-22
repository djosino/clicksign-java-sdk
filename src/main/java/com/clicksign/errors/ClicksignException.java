package com.clicksign.errors;

/**
 * Base exception for all Clicksign SDK errors.
 *
 * <p>HTTP errors expose {@link #statusCode()}, {@link #requestId()} and {@link #responseBody()}.
 */
public class ClicksignException extends RuntimeException {

    /** HTTP status code. */
    private final int statusCode;
    /** API request id. */
    private final String requestId;
    /** Raw response body. */
    private final String responseBody;

    /** Constructs a new exception.
     * @param message detail message
     * @param statusCode HTTP status code
     * @param requestId API request id
     * @param responseBody raw response body
     */
    public ClicksignException(String message, int statusCode, String requestId, String responseBody) {
        super(message);
        this.statusCode   = statusCode;
        this.requestId    = requestId;
        this.responseBody = responseBody;
    }

    /** Constructs a new exception.
     * @param message detail message
     * @param cause the cause
     */
    public ClicksignException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode   = 0;
        this.requestId    = null;
        this.responseBody = null;
    }

    /**
     * Returns the status code.
     *
     * @return status code
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the request id.
     *
     * @return request id
     */
    public String requestId() {
        return requestId;
    }

    /**
     * Returns the response body.
     *
     * @return response body
     */
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
