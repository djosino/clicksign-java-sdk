# Retries e timeouts

**Quando usar:** chamadas em produção sujeitas a instabilidade de rede, rate limit (429) ou erros transitórios (5xx).

## Código

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.RateLimitException;
import com.clicksign.errors.ServerException;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.PRODUCTION)
    .maxRetries(3)
    .connectTimeoutMs(3_000)
    .readTimeoutMs(15_000)
    .onRetry(event -> System.out.printf("retry %d, aguardando %dms%n",
        event.attempt(), event.waitMs()))
    .build();

try {
    client.envelopes().retrieve("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
} catch (RateLimitException e) {
    // isRetryable() == true — SDK já tentou maxRetries vezes
    Long retryAfter = e.retryAfterSeconds();
} catch (ServerException e) {
    if (e.isRetryable()) { /* ... */ }
} catch (ClicksignException e) {
    // erros 4xx não são retryable
}
```

## O que está acontecendo

- `maxRetries(3)` permite até 3 **novas** tentativas após a primeira falha retryable
- Backoff full jitter: espera aleatória em `[0, min(0.5×2^(n−1), 30)]` segundos
- `429` e `503` podem usar o header `Retry-After` da API
- `onRetry` dispara antes de cada espera — útil para métricas

## Erros comuns

- Esquecer `maxRetries` (padrão `0`) e esperar retry automático em 500
- Tratar `ValidationException` com retry na aplicação — não é retryable e reenviar piora o problema
- Configurar retry alto em bulk — `BulkOperationsClient` só retenta timeout, não 5xx ([08-production-limitations.md](08-production-limitations.md))

Contrato completo: [SDK_CONTRACT.md](../SDK_CONTRACT.md#7-retry-http-principal).
