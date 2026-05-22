package com.clicksign.errors;

/** Raised when webhook HMAC signature validation fails. */
public class WebhookSignatureException extends ClicksignException {

    public WebhookSignatureException(String message) {
        super(message, 0, null, null);
    }
}
