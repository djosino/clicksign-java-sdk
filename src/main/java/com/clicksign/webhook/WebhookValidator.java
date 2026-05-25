package com.clicksign.webhook;

import com.clicksign.errors.WebhookSignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * HMAC-SHA256 webhook signature validation.
 *
 * <p>The {@code signature} parameter in all methods must be the full header value in the form
 * {@code sha256=<64-hex-chars>} as sent by Clicksign. Passing a bare hex string (without the
 * {@code sha256=} prefix) will always fail validation.
 *
 * <pre>{@code
 * WebhookValidator.verifySignature(payload, signatureHeader, secret);
 * // or without throwing:
 * boolean valid = WebhookValidator.isValidSignature(payload, signatureHeader, secret);
 * }</pre>
 */
public final class WebhookValidator {

    private static final String ALGORITHM  = "HmacSHA256";
    private static final String PREFIX     = "sha256=";
    private static final int    MAX_SIG_LEN = 1024;

    private WebhookValidator() {}

    /**
     * Verifies the webhook signature.
     *
     * @param payload   raw request body
     * @param signature value of the {@code X-Clicksign-Hmac-SHA256} header (must include
     *                  the {@code sha256=} prefix)
     * @param secret    webhook secret configured in the Clicksign dashboard; must not be blank
     * @throws WebhookSignatureException if signature is null/blank/wrong-format, payload is null,
     *                                   secret is null/blank, or the signature does not match
     */
    public static void verifySignature(String payload, String signature, String secret) {
        if (signature == null || signature.isBlank()) {
            throw new WebhookSignatureException("Webhook signature is missing");
        }
        String normalizedSig = signature.toLowerCase(Locale.ROOT);
        if (!normalizedSig.startsWith(PREFIX)) {
            throw new WebhookSignatureException(
                "Webhook signature has invalid format — expected 'sha256=<hex>'");
        }
        if (payload == null) {
            throw new WebhookSignatureException("Webhook payload must not be null");
        }
        if (secret == null || secret.isBlank()) {
            throw new WebhookSignatureException("Webhook secret must not be null or blank");
        }
        String expected = computeSignature(payload, secret);
        if (!secureEqual(expected, normalizedSig)) {
            throw new WebhookSignatureException("Webhook signature mismatch");
        }
    }

    /**
     * Returns true if valid, false otherwise — does not throw.
     *
     * @param payload   raw request body
     * @param signature value of the {@code X-Clicksign-Hmac-SHA256} header
     * @param secret    webhook secret configured in the Clicksign dashboard
     * @return {@code true} if the signature is valid
     */
    public static boolean isValidSignature(String payload, String signature, String secret) {
        if (payload == null || secret == null || secret.isBlank()) {
            return false;
        }
        try {
            verifySignature(payload, signature, secret);
            return true;
        } catch (WebhookSignatureException e) {
            return false;
        }
    }

    /**
     * Computes the expected {@code sha256=<hex>} signature for a given payload and secret.
     *
     * @param payload raw request body; must not be null
     * @param secret  webhook secret; must not be null or blank
     * @return expected signature string in {@code sha256=<hex>} format
     * @throws IllegalArgumentException if payload or secret is null or secret is blank
     */
    public static String computeSignature(String payload, String secret) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("secret must not be null or blank");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return PREFIX + bytesToHex(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }

    /**
     * Constant-time comparison — prevents timing attacks.
     * Both sides are hashed to fixed-length SHA-256 digests (32 bytes each) before
     * calling {@link MessageDigest#isEqual}, ensuring the comparison always iterates
     * exactly 32 bytes regardless of input length.
     * Input {@code b} is rejected early (O(1)) if longer than {@value MAX_SIG_LEN} chars
     * to prevent DoS via unbounded hashing; this early-return creates a timing difference
     * between "oversized" and "wrong" signatures, which is an accepted trade-off.
     */
    private static boolean secureEqual(String a, String b) {
        if (b.length() > MAX_SIG_LEN) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] digestA = digest.digest(a.getBytes(StandardCharsets.UTF_8));
            byte[] digestB = digest.digest(b.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(digestA, digestB);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
