package com.clicksign.errors;

/** Raised on HTTP 409. */
public class ConflictException extends ClicksignException {
    /** Constructs a new 409 Conflict exception.
     * @param message detail message
     * @param statusCode HTTP status code
     * @param requestId API request id
     * @param responseBody raw response body
     */
    public ConflictException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
