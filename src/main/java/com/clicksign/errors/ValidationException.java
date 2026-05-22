package com.clicksign.errors;

/** Raised on HTTP 400 or 422. */
public class ValidationException extends ClicksignException {
    public ValidationException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
