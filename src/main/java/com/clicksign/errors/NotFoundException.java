package com.clicksign.errors;

/** Raised on HTTP 404. */
public class NotFoundException extends ClicksignException {
    public NotFoundException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }
}
