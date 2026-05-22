# Exemplos compiláveis

Código Java espelhando o cookbook em [docs/examples/](../docs/examples/).

| Classe | Documento |
|--------|-----------|
| `RetriesExample` | [01-retries.md](../docs/examples/01-retries.md) |
| `BulkRequirementsExample` | [02-bulk-requirements.md](../docs/examples/02-bulk-requirements.md) |
| `WebhooksExample` | [03-webhooks.md](../docs/examples/03-webhooks.md) |
| `MultiClientExample` | [04-multi-client.md](../docs/examples/04-multi-client.md) |
| `ListAndFilterExample` | [07-list-and-filter.md](../docs/examples/07-list-and-filter.md) |
| `ProductionLimitationsExample` | [08-production-limitations.md](../docs/examples/08-production-limitations.md) |

## Compilar (CI)

```bash
./gradlew :examples:compileJava
# ou, com verificação completa do projeto:
./gradlew check
```

## Executar

A maioria dos exemplos exige `CLICKSIGN_API_KEY` (sandbox ou produção).

```bash
export CLICKSIGN_API_KEY=seu-token

./gradlew :examples:runRetries
./gradlew :examples:runBulkRequirements
./gradlew :examples:runListAndFilter
# ...

# Webhook (validação local, sem API key obrigatória):
./gradlew :examples:runWebhooks

# Multi-tenant (só instancia clientes, sem chamada HTTP):
./gradlew :examples:runMultiClient
```
