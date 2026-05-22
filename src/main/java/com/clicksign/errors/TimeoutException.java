package com.clicksign.errors;

/** Raised on connection or read timeout. Retryable. */
public class TimeoutException extends ClicksignException {

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
