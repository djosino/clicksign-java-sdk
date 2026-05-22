package com.clicksign.errors;

/** Raised on connection or read timeout. Retryable. */
public class TimeoutException extends ClicksignException {

    /**
     * Constructs a new timeout exception.
     *
     * @param message detail message
     * @param cause   the cause
     */
    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
