# Troubleshooting

Sintomas comuns ao integrar o Clicksign Java SDK — causa e correção.

## Tabela rápida de erros HTTP

| HTTP | Exceção Java | `isRetryable()` |
|------|-------------|-----------------|
| 401, 403 | `AuthenticationException` | não |
| 404 | `NotFoundException` | não |
| 400, 422 | `ValidationException` | não |
| 409 | `ConflictException` | não |
| 429 | `RateLimitException` | sim |
| 503 | `ServiceUnavailableException` | sim |
| 5xx | `ServerException` | sim |
| Timeout / rede | `TimeoutException` | sim |
| Assinatura webhook inválida | `WebhookSignatureException` | não |

---

### `IllegalStateException: apiKey is required` na primeira chamada

**Causa:** `ClicksignClient.builder().build()` sem `apiKey()` ou com string em branco.

**Correção:**

```java
ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY")) // nunca hardcode
    .environment(Environment.SANDBOX)
    .build();
```

---

### `AuthenticationException` em todas as requisições

**Causa:** token inválido, expirado, ou ambiente errado (token de produção no sandbox ou vice-versa).

**Correção:** gere um novo token no painel correspondente ao `Environment` configurado.

---

### `IllegalArgumentException` ao montar `CreateParams`

**Causa:** validação client-side nos builders (antes do HTTP).

**Correção:** exemplos frequentes:

```java
// Nome sem sobrenome
Signer.CreateParams.builder().name("João")  // exige "João Silva"

// WhatsApp em communicate_events sem telefone
Signer.CreateParams.builder()
    .communicateEvents(Map.of("signature_request", "whatsapp"))
    .phoneNumber(null)  // phoneNumber obrigatório

// Envelope com locale inválido
Envelope.CreateParams.builder().locale("pt_BR")  // use "pt-BR" ou EnvelopeLocale.PT_BR
```

### `ValidationException` com mensagem da API

**Causa:** atributo inválido ou ausente no payload enviado à API.

**Correção:** leia `responseBody()` e a mensagem da exceção. Confira [campos e regras do envelope](https://developers.clicksign.com/reference/envelope-campos-e-regras-de-negocio.md) na documentação oficial.

Para erros 422 da API, confira [campos e regras](https://developers.clicksign.com/reference/envelope-campos-e-regras-de-negocio.md) na documentação oficial.

---

### `BulkRequirement` retorna sucesso HTTP mas alguns slots falham

**Causa:** respostas atômicas podem trazer `atomic:results` com erros por índice sem lançar exceção global.

**Correção:**

```java
BulkRequirement.Response response = client.bulkRequirements().create(envelopeId, ops -> ops
    .addAgree(signerId, documentId, "sign"));

if (!response.isSuccess()) {
    for (BulkRequirement.OperationResult failure : response.failures()) {
        System.err.println("op=" + failure.op() + " index=" + failure.index());
        failure.errors().forEach(err -> System.err.println(err));
    }
}
```

---

### `WebhookSignatureException` ou `isValidSignature` retorna false

**Causa:** secret diferente do configurado no webhook, payload alterado (proxy, re-encoding), ou header de assinatura ausente.

**Correção:**

```java
import com.clicksign.webhook.WebhookValidator;

String payload = rawRequestBody; // bytes exatos recebidos
String signature = request.getHeader("X-Clicksign-Signature"); // nome conforme sua configuração

boolean ok = WebhookValidator.isValidSignature(payload, signature, webhookSecret);
```

Use o corpo **bruto** da requisição — não re-serialize JSON.

---

### Retry não acontece em erro 500

**Causa:** `maxRetries` padrão é `0` (sem retry).

**Correção:**

```java
ClicksignClient.builder()
    .apiKey(apiKey)
    .maxRetries(3)
    .build();
```

---

### `RateLimitException` mesmo com retry configurado

**Causa:** limite da conta excedido; retries esgotados ou intervalo insuficiente.

**Correção:** reduza taxa de chamadas na aplicação. Verifique `retryAfterSeconds()` na exceção:

```java
} catch (RateLimitException e) {
    Long wait = e.retryAfterSeconds();
    // aplicar backoff na aplicação se necessário
}
```

---

### `Folder` não possui `update` ou `delete`

**Causa:** a API 3.0 expõe apenas listagem, detalhe e criação de pastas — não há endpoints de update/delete no escopo atual do SDK.

**Correção:** crie nova pasta ou reorganize via relacionamento em `Folder.CreateParams.folderId()`.

---

### Erro 405 ao atualizar `Membership`

**Causa:** integrações antigas usavam PATCH; a API exige PUT.

**Correção:** o SDK já usa PUT em `Membership.Service.update()` — atualize para a versão atual do SDK.

---

### `NullPointerException` em `statusAsEnum()` ou enums tipados

**Causa:** confusão — `statusAsEnum()` retorna `null` para valores novos da API ainda não mapeados no SDK, não lança NPE. O NPE ocorre se você chamar método no enum sem verificar null.

**Correção:**

```java
EnvelopeStatus status = envelope.statusAsEnum();
if (status == EnvelopeStatus.RUNNING) { ... }
// ou use envelope.status() para string bruta
```

---

### Testes de integração sandbox não rodam

**Causa:** testes `@Tag("integration")` são excluídos por padrão.

**Correção:**

```bash
CLICKSIGN_API_KEY=seu-token ./gradlew test -PincludeIntegration \
  --tests "*.SandboxIntegrationTest"
```
