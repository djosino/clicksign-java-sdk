package com.clicksign.errors;

/** Raised on HTTP 5xx. Retryable. */
public class ServerException extends ClicksignException {

    public ServerException(String message, int statusCode, String requestId, String responseBody) {
        super(message, statusCode, requestId, responseBody);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
