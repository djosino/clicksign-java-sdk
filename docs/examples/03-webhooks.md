# Validação de webhooks

**Quando usar:** endpoint HTTP na sua aplicação que recebe callbacks da Clicksign e precisa garantir autenticidade do payload.

## Código

```java
import com.clicksign.errors.WebhookSignatureException;
import com.clicksign.webhook.WebhookValidator;

// No controller/servlet — use o corpo BRUTO da requisição
public void handleWebhook(String rawBody, String signatureHeader, String webhookSecret) {
    try {
        WebhookValidator.verifySignature(rawBody, signatureHeader, webhookSecret);
        // processar evento JSON...
    } catch (WebhookSignatureException e) {
        // responder 401
    }
}

// Alternativa sem exceção
boolean valid = WebhookValidator.isValidSignature(rawBody, signatureHeader, webhookSecret);
if (!valid) { return; }

// Gerar assinatura esperada (testes)
String expected = WebhookValidator.computeSignature(rawBody, webhookSecret);
```

Criar webhook com secret na API:

```java
import com.clicksign.resources.Webhook;
import com.clicksign.resources.types.WebhookEventType;

client.webhooks().create(Webhook.CreateParams.builder()
    .endpoint("https://sua-app.example/webhooks/clicksign")
    .addEvent(WebhookEventType.SIGN)
    .addEvent(WebhookEventType.CLOSE)
    .secret(webhookSecret)
    .build());
```

## O que está acontecendo

- HMAC-SHA256 do payload UTF-8 com o secret do webhook
- Formato: `sha256=<hex>`
- Comparação em tempo constante (mitiga timing attacks)

## Executar o exemplo

```bash
export CLICKSIGN_WEBHOOK_SECRET=meu-secret   # opcional
./gradlew :examples:runWebhooks
```

Saída esperada (sem registrar na API):

```
1) Assinatura válida (compute + verify): OK
2) Assinatura inválida rejeitada (esperado): OK
3) Registro na API omitido. Para tentar: CLICKSIGN_REGISTER_WEBHOOK=true
```

`CLICKSIGN_REGISTER_WEBHOOK=true` tenta criar webhook no sandbox — se a conta retornar *secret não está disponível*, isso é limitação da conta, não do SDK.

## Erros comuns

- Validar JSON re-serializado em vez do corpo original — a assinatura quebra
- Usar secret do ambiente errado (sandbox vs produção)
- Logar o secret ou o header `Authorization` da API no mesmo pipeline de logs do webhook

> **Segurança:** trate o `webhookSecret` como credencial. Rotacione se houver vazamento.

Contrato: [SDK_CONTRACT.md](../SDK_CONTRACT.md#10-webhook).
