# Observabilidade

Como instrumentar requisições, retries e erros do Clicksign Java SDK em produção.

## Hooks de instrumentação

Registre listeners no **builder** do cliente (antes de `build()`):

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.instrumentation.RequestEvent;
import com.clicksign.instrumentation.RetryEvent;
import com.clicksign.instrumentation.ErrorEvent;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .maxRetries(3)
    .onRequest(event -> {
        System.out.printf("[Clicksign] %s %s → %d (%dms, tentativa %d)%n",
            event.method(), event.path(), event.status(),
            (long) event.durationMs(), event.attempt());
    })
    .onRetry(event -> {
        System.out.printf("[Clicksign] retry %d/%d em %dms — %s%n",
            event.attempt(), event.maxRetries(), event.waitMs(),
            event.error().getClass().getSimpleName());
    })
    .onError(event -> {
        System.err.printf("[Clicksign] erro %s %s status=%d (%dms): %s%n",
            event.method(), event.path(), event.status(),
            (long) event.durationMs(), event.error().getMessage());
    })
    .build();
```

> **Nota:** não existe `client.instrumentation()` após o build — os hooks são configurados apenas no builder.

## Campos dos eventos

### Ordem dos eventos

Para cada tentativa HTTP no `HttpClient` principal:

1. Resposta recebida → `onRequest` (sempre, com status HTTP real)
2. Se status ≥ 400 → `onError` e exceção lançada ao caller
3. Se retryable e tentativas restantes → `onRetry`, depois nova tentativa

Em timeout de rede, só `onRetry` (até esgotar) e depois `onError`.

### `RequestEvent`

Disparado após cada resposta HTTP recebida (2xx, 4xx e 5xx).

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `method()` | `String` | GET, POST, PATCH, PUT, DELETE |
| `path()` | `String` | Path relativo (ex.: `/envelopes/{id}`) |
| `status()` | `int` | Status HTTP |
| `attempt()` | `int` | Número da tentativa (1 = primeira) |
| `durationMs()` | `double` | Duração da requisição em milissegundos |

### `RetryEvent`

Disparado **antes** de aguardar e repetir uma requisição retryable.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `method()` | `String` | Método HTTP |
| `path()` | `String` | Path relativo |
| `attempt()` | `int` | Tentativa que acabou de falhar |
| `maxRetries()` | `int` | Valor de `maxRetries` do builder |
| `error()` | `Throwable` | Exceção que motivou o retry |
| `waitMs()` | `long` | Tempo de espera até a próxima tentativa |

### `ErrorEvent`

Disparado quando a requisição termina com exceção lançada ao caller.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `method()` | `String` | Método HTTP |
| `path()` | `String` | Path relativo |
| `status()` | `int` | Status HTTP, ou `0` para timeout/rede |
| `error()` | `Throwable` | Exceção (`ClicksignException` ou causa) |
| `durationMs()` | `double` | Duração até a falha |

## Correlation id

A API pode retornar o header `x-request-id`. Em erros HTTP, o valor fica em `ClicksignException.requestId()`:

```java
try {
    client.envelopes().retrieve("id-inexistente");
} catch (com.clicksign.errors.NotFoundException e) {
    logger.error("request_id={} body={}", e.requestId(), e.responseBody());
}
```

Use esse ID ao abrir chamado com o suporte Clicksign. O SDK **não envia** hoje um `X-Request-Id` customizado na requisição — apenas lê o da resposta.

## Integração com SLF4J

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

Logger log = LoggerFactory.getLogger("clicksign");

ClicksignClient client = ClicksignClient.builder()
    .apiKey(apiKey)
    .environment(Environment.PRODUCTION)
    .onRequest(e -> log.info("clicksign method={} path={} status={} duration_ms={}",
        e.method(), e.path(), e.status(), (long) e.durationMs()))
    .onRetry(e -> log.warn("clicksign retry attempt={} wait_ms={} error={}",
        e.attempt(), e.waitMs(), e.error().toString()))
    .onError(e -> log.error("clicksign failed method={} path={} status={} msg={}",
        e.method(), e.path(), e.status(), e.error().getMessage(), e.error()))
    .build();
```

## Micrometer (receita)

Registre latência por requisição no `onRequest`:

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

MeterRegistry registry = ...;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(apiKey)
    .onRequest(e -> registry.timer("clicksign.http",
            "method", e.method(),
            "status", String.valueOf(e.status()))
        .record((long) e.durationMs(), java.util.concurrent.TimeUnit.MILLISECONDS))
    .onRetry(e -> registry.counter("clicksign.retry",
            "method", e.method()).increment())
    .build();
```

Adapte tags conforme cardinalidade — evite `path` com UUIDs dinâmicos; prefira templates como `envelopes/{id}`.

> **Segurança:** nunca registre o header `Authorization`, tokens, CPF, e-mail ou conteúdo de documentos nos logs. Os hooks expõem apenas método, path e status.

## Bulk e instrumentação

`BulkOperationsClient` **não** publica eventos de `Instrumentation` hoje — apenas o `HttpClient` principal. Monitore bulk via logs da aplicação em torno de `bulkRequirements().create()` e inspeção de `BulkRequirement.Response.failures()`.
