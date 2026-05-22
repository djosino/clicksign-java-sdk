# Limitações em produção

**Quando usar:** revisão de arquitetura antes de deploy — entender thread safety, HTTP e ausência de async.

## Código

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;

// Uma instância por processo/conta — reutilize entre threads
ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.PRODUCTION)
    .maxRetries(3)
    .readTimeoutMs(30_000)
    .build();

// Paralelismo na aplicação (não no SDK)
java.util.concurrent.ExecutorService pool =
    java.util.concurrent.Executors.newFixedThreadPool(8);

pool.submit(() -> client.envelopes().retrieve("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
```

## O que está acontecendo

| Tópico | Comportamento |
|--------|----------------|
| HTTP | `java.net.http.HttpClient` interno por `ClicksignClient` |
| Connection pool | Não configurável na API pública; JVM gerencia keep-alive |
| Thread safety | `ClicksignClient` imutável — seguro compartilhar entre threads |
| Async | **Não implementado** — use `ExecutorService` / virtual threads na app |
| Bulk retry | Apenas `TimeoutException`, não 5xx |
| Instrumentação | Hooks no builder; bulk não emite eventos hoje |

## Erros comuns

- Criar novo `ClicksignClient` a cada requisição — desperdiça conexões; reuse instância
- Bloquear thread do servidor web com muitas chamadas síncronas — use pool com limite
- Esperar retry automático em bulk após 500 — pode duplicar operações já aplicadas
- Logar `apiKey` em listeners `onRequest` — nunca logue `Authorization`

Observabilidade: [OBSERVABILITY.md](../OBSERVABILITY.md).

Arquitetura: [ARCHITECTURE.md](../ARCHITECTURE.md).
