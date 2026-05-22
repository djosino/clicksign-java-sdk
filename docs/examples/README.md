# Exemplos (cookbook)

Receitas curtas e copiáveis. Cada arquivo segue o formato: **quando usar** → **código** → **o que acontece** → **erros comuns**.

**Código compilável:** as mesmas receitas existem em Java em [examples/](../../examples/) (`./gradlew :examples:compileJava`).

| Arquivo | Tema |
|---------|------|
| [01-retries.md](01-retries.md) | `maxRetries`, backoff, `TimeoutException` |
| [02-bulk-requirements.md](02-bulk-requirements.md) | `BulkRequirement`, falhas por slot |
| [03-webhooks.md](03-webhooks.md) | Validação HMAC de webhooks |
| [04-multi-client.md](04-multi-client.md) | Multi-tenant, Spring DI |
| [07-list-and-filter.md](07-list-and-filter.md) | `ResourceQuery`, `fetchAll()` |
| [08-production-limitations.md](08-production-limitations.md) | Thread safety, HTTP client, async |

Fluxo completo: [WORKFLOW.md](../WORKFLOW.md).
