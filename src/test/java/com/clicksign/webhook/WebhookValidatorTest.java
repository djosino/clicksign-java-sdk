package com.clicksign.webhook;

import com.clicksign.errors.WebhookSignatureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookValidatorTest {

    private static final String SECRET  = "my-webhook-secret";
    private static final String PAYLOAD = "{\"event\":\"sign\",\"data\":{}}";

    @Test
    void computeSignatureReturnsSha256PrefixedHex() {
        String sig = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        assertTrue(sig.startsWith("sha256="));
        assertEquals(7 + 64, sig.length());
    }

    @Test
    void computeSignatureIsDeterministic() {
        String a = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        String b = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        assertEquals(a, b);
    }

    @Test
    void computeSignatureDiffersForDifferentPayload() {
        String a = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        String b = WebhookValidator.computeSignature("{\"event\":\"close\"}", SECRET);
        assertNotEquals(a, b);
    }

    @Test
    void verifySignaturePassesForValidSignature() {
        String sig = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        assertDoesNotThrow(() -> WebhookValidator.verifySignature(PAYLOAD, sig, SECRET));
    }

    @Test
    void verifySignatureThrowsForInvalidSignature() {
        assertThrows(WebhookSignatureException.class,
            () -> WebhookValidator.verifySignature(PAYLOAD, "sha256=bad", SECRET));
    }

    @Test
    void verifySignatureThrowsForNullSignature() {
        assertThrows(WebhookSignatureException.class,
            () -> WebhookValidator.verifySignature(PAYLOAD, null, SECRET));
    }

    @Test
    void verifySignatureThrowsForEmptySignature() {
        assertThrows(WebhookSignatureException.class,
            () -> WebhookValidator.verifySignature(PAYLOAD, "", SECRET));
    }

    @Test
    void verifySignatureThrowsForTamperedPayload() {
        String sig = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        assertThrows(WebhookSignatureException.class,
            () -> WebhookValidator.verifySignature("{\"tampered\":true}", sig, SECRET));
    }

    @Test
    void verifySignatureThrowsForWrongSecret() {
        String sig = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        assertThrows(WebhookSignatureException.class,
            () -> WebhookValidator.verifySignature(PAYLOAD, sig, "wrong-secret"));
    }

    @Test
    void isValidSignatureReturnsTrueForValid() {
        String sig = WebhookValidator.computeSignature(PAYLOAD, SECRET);
        assertTrue(WebhookValidator.isValidSignature(PAYLOAD, sig, SECRET));
    }

    @Test
    void isValidSignatureReturnsFalseForInvalid() {
        assertFalse(WebhookValidator.isValidSignature(PAYLOAD, "sha256=bad", SECRET));
    }

    @Test
    void isValidSignatureReturnsFalseForNull() {
        assertFalse(WebhookValidator.isValidSignature(PAYLOAD, null, SECRET));
    }

    @Test
    void verifySignatureThrowsForWrongAlgorithmPrefix() {
        String validHmac = WebhookValidator.computeSignature(PAYLOAD, SECRET).substring(7);
        assertThrows(WebhookSignatureException.class,
            () -> WebhookValidator.verifySignature(PAYLOAD, "sha1=" + validHmac, SECRET));
    }

    @Test
    void isValidSignatureReturnsFalseForWrongAlgorithmPrefix() {
        String validHmac = WebhookValidator.computeSignature(PAYLOAD, SECRET).substring(7);
        assertFalse(WebhookValidator.isValidSignature(PAYLOAD, "sha1=" + validHmac, SECRET));
    }
}
