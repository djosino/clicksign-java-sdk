package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.errors.WebhookSignatureException;
import com.clicksign.resources.Webhook;
import com.clicksign.resources.types.WebhookEventType;
import com.clicksign.webhook.WebhookValidator;

/** Espelha {@code docs/examples/03-webhooks.md}. */
public final class WebhooksExample {

    public static void main(String[] args) {
        String rawBody = args.length > 0 ? args[0] : "{\"event\":\"sign\"}";
        String signatureHeader = args.length > 1 ? args[1] : "sha256=deadbeef";
        String webhookSecret = args.length > 2 ? args[2] : "test-secret";

        try {
            WebhookValidator.verifySignature(rawBody, signatureHeader, webhookSecret);
            System.out.println("assinatura válida");
        } catch (WebhookSignatureException e) {
            System.out.println("assinatura inválida (esperado em demo): " + e.getMessage());
        }

        boolean valid = WebhookValidator.isValidSignature(rawBody, signatureHeader, webhookSecret);
        String expected = WebhookValidator.computeSignature(rawBody, webhookSecret);
        System.out.println("isValid=" + valid + " expectedPrefix=" + expected.substring(0, Math.min(20, expected.length())));

        String apiKey = System.getenv("CLICKSIGN_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            ClicksignClient client = ClicksignClient.builder()
                .apiKey(apiKey)
                .environment(Environment.SANDBOX)
                .build();
            client.webhooks().create(Webhook.CreateParams.builder()
                .endpoint("https://sua-app.example/webhooks/clicksign")
                .addEvent(WebhookEventType.SIGN)
                .addEvent(WebhookEventType.CLOSE)
                .secret(webhookSecret)
                .build());
        }
    }
}
