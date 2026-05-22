package com.clicksign.errors;

/** Raised on HTTP 401 or 403. */
public class AuthenticationException extends ClicksignException {
    public AuthenticationException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
