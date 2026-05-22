package com.clicksign.webhook;

import com.clicksign.errors.WebhookSignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC-SHA256 webhook signature validation.
 *
 * <pre>{@code
 * WebhookValidator.verifySignature(payload, signatureHeader, secret);
 * // or without throwing:
 * boolean valid = WebhookValidator.isValidSignature(payload, signatureHeader, secret);
 * }</pre>
 */
public final class WebhookValidator {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String PREFIX    = "sha256=";

    private WebhookValidator() {}

    /**
     * Verifies the webhook signature.
     *
     * @throws WebhookSignatureException if signature is null, empty, or does not match.
     */
    public static void verifySignature(String payload, String signature, String secret) {
        if (signature == null || signature.isBlank()) {
            throw new WebhookSignatureException("Webhook signature is missing");
        }
        String expected = computeSignature(payload, secret);
        if (!secureEqual(expected, signature)) {
            throw new WebhookSignatureException("Webhook signature mismatch");
        }
    }

    /** Returns true if valid, false otherwise — does not throw. */
    public static boolean isValidSignature(String payload, String signature, String secret) {
        try {
            verifySignature(payload, signature, secret);
            return true;
        } catch (WebhookSignatureException e) {
            return false;
        }
    }

    /** Computes the expected {@code sha256=<hex>} signature for a given payload and secret. */
    public static String computeSignature(String payload, String secret) {
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
     * Hashes both strings with SHA-256 before comparing so length differences do not leak.
     */
    private static boolean secureEqual(String a, String b) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] digestA = digest.digest(a.getBytes(StandardCharsets.UTF_8));
            digest.reset();
            byte[] digestB = digest.digest(b.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(digestA, digestB);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
