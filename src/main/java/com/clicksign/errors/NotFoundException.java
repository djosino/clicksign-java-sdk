package com.clicksign.errors;

/** Raised on HTTP 404. */
public class NotFoundException extends ClicksignException {
    /**
     * Constructs a new 404 Not Found exception.
     *
     * @param message      detail message
     * @param statusCode   HTTP status code
     * @param requestId    API request id
     * @param responseBody raw response body
     */
    public NotFoundException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
