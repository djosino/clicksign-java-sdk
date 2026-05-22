package com.clicksign.errors;

/** Raised on HTTP 401 or 403. */
public class AuthenticationException extends ClicksignException {
    /**
     * Constructs an AuthenticationException.
     *
     * @param message error message
     * @param statusCode HTTP status code (401 or 403)
     * @param requestId request identifier from the API response
     * @param responseBody raw response body
     */
    public AuthenticationException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
