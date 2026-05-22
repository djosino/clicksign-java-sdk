package com.clicksign.errors;

/** Raised on HTTP 409. */
public class ConflictException extends ClicksignException {
    public ConflictException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
