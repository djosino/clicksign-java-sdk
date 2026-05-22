package com.clicksign.errors;

/** Raised when webhook HMAC signature validation fails. */
public class WebhookSignatureException extends ClicksignException {

    /**
     * Constructs a new webhook signature exception.
     *
     * @param message detail message
     */
    public WebhookSignatureException(String message) {
        super(message, 0, null, null);
    }
}
