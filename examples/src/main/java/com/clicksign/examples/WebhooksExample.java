package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.WebhookSignatureException;
import com.clicksign.resources.Webhook;
import com.clicksign.resources.types.WebhookEventType;
import com.clicksign.webhook.WebhookValidator;

/**
 * Demonstra validação HMAC de webhooks (sempre local).
 * Registro na API é opcional — muitas contas sandbox retornam
 * {@code secret não está disponível}.
 */
public final class WebhooksExample {

    public static void main(String[] args) {
        String webhookSecret = System.getenv("CLICKSIGN_WEBHOOK_SECRET");
        if (webhookSecret == null || webhookSecret.isBlank()) {
            webhookSecret = "test-secret";
        }

        String rawBody = args.length > 0 ? args[0]
            : System.getenv().getOrDefault("CLICKSIGN_WEBHOOK_BODY", "{\"event\":\"sign\"}");

        // 1) Fluxo feliz: gerar assinatura correta e validar
        String validSignature = WebhookValidator.computeSignature(rawBody, webhookSecret);
        try {
            WebhookValidator.verifySignature(rawBody, validSignature, webhookSecret);
            System.out.println("1) Assinatura válida (compute + verify): OK");
        } catch (WebhookSignatureException e) {
            System.err.println("1) FALHA inesperada: " + e.getMessage());
            System.exit(1);
        }

        // 2) Assinatura errada de propósito
        try {
            WebhookValidator.verifySignature(rawBody, "sha256=deadbeef", webhookSecret);
            System.err.println("2) FALHA: deveria rejeitar assinatura inválida");
            System.exit(1);
        } catch (WebhookSignatureException e) {
            System.out.println("2) Assinatura inválida rejeitada (esperado): OK");
        }

        System.out.println("isValid(invalid)=" + WebhookValidator.isValidSignature(
            rawBody, "sha256=deadbeef", webhookSecret));

        // 3) Opcional: criar webhook na API (pode falhar se secret não estiver habilitado na conta)
        if (!"true".equalsIgnoreCase(System.getenv("CLICKSIGN_REGISTER_WEBHOOK"))) {
            System.out.println("3) Registro na API omitido. Para tentar: CLICKSIGN_REGISTER_WEBHOOK=true");
            return;
        }

        String apiKey = System.getenv("CLICKSIGN_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("3) CLICKSIGN_API_KEY ausente — pulando registro na API");
            return;
        }

        try {
            ClicksignClient client = ClicksignClient.builder()
                .apiKey(apiKey)
                .environment(ExampleSupport.environment())
                .build();
            Webhook webhook = client.webhooks().create(Webhook.CreateParams.builder()
                .endpoint("https://webhook.site/unique-id-substitua")
                .addEvent(WebhookEventType.SIGN)
                .addEvent(WebhookEventType.CLOSE)
                .secret(webhookSecret)
                .build());
            System.out.println("3) Webhook criado na API: id=" + webhook.id());
        } catch (ClicksignException e) {
            System.out.println("3) Registro na API falhou (comum em sandbox): " + e.getMessage());
            System.out.println("   Validação HMAC (passos 1–2) não depende da API.");
        }
    }
}
