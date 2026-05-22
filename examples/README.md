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

## Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|----------|-------------|--------|-----------|
| `CLICKSIGN_API_KEY` | Sim* | — | Access token da conta |
| `CLICKSIGN_ENVIRONMENT` | Não | `sandbox` | `sandbox` ou `production` |

\* `runWebhooks` não precisa de API key para os passos 1–2 (HMAC local). Passo 3 (criar webhook) é opcional.

| Variável extra (`runWebhooks`) | Padrão | Descrição |
|-------------------------------|--------|-----------|
| `CLICKSIGN_WEBHOOK_SECRET` | `test-secret` | Secret usado no HMAC de demonstração |
| `CLICKSIGN_WEBHOOK_BODY` | `{"event":"sign"}` | Payload bruto para validar |
| `CLICKSIGN_REGISTER_WEBHOOK` | (off) | `true` tenta `webhooks().create()` — pode falhar se a conta não suporta `secret` |

**Não é necessário passar parâmetros no Gradle** — só exportar as variáveis no mesmo shell:

```bash
export CLICKSIGN_API_KEY="seu-token-sandbox"
export CLICKSIGN_ENVIRONMENT=sandbox   # opcional; já é o padrão

./gradlew :examples:runRetries
```

Para produção:

```bash
export CLICKSIGN_API_KEY="token-producao"
export CLICKSIGN_ENVIRONMENT=production
./gradlew :examples:runRetries
```

## Compilar (CI)

```bash
./gradlew :examples:compileJava
```

## Executar todos

```bash
export CLICKSIGN_API_KEY=seu-token

./gradlew :examples:runWebhooks
# opcional: tentar criar webhook na API (pode retornar "secret não está disponível")
# CLICKSIGN_REGISTER_WEBHOOK=true ./gradlew :examples:runWebhooks
./gradlew :examples:runMultiClient
./gradlew :examples:runListAndFilter
./gradlew :examples:runBulkRequirements
./gradlew :examples:runRetries
./gradlew :examples:runProductionLimitations
```

Listar tarefas: `./gradlew tasks --group=examples`
