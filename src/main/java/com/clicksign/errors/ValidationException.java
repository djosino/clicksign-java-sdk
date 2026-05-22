package com.clicksign.errors;

/** Raised on HTTP 400 or 422. */
public class ValidationException extends ClicksignException {
    /**
     * Constructs a new 400/422 Validation exception.
     *
     * @param message      detail message
     * @param statusCode   HTTP status code
     * @param requestId    API request id
     * @param responseBody raw response body
     */
    public ValidationException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
