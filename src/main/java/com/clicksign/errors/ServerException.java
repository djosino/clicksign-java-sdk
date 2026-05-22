package com.clicksign.errors;

/** Raised on HTTP 5xx. Retryable. */
public class ServerException extends ClicksignException {

    /**
     * Constructs a new 5xx Server exception.
     *
     * @param message      detail message
     * @param statusCode   HTTP status code
     * @param requestId    API request id
     * @param responseBody raw response body
     */
    public ServerException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
