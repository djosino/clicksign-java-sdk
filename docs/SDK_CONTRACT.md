# Contrato do SDK (API 3.0)

Comportamento compartilhado entre os SDKs Clicksign (Python, Ruby, Java). Este documento é agnóstico de linguagem; a coluna Java indica como o contrato se manifesta neste repositório.

## 1. Autenticação

| Regra | Detalhe |
|-------|---------|
| Header | `Authorization: <access_token>` |
| Formato | **Sem** prefixo `Bearer` |
| Java | `ClientConfig.apiKey()` → header `Authorization` |

> **Segurança:** nunca commite tokens; use variáveis de ambiente ou secret manager.

## 2. URLs base

| Ambiente | URL |
|----------|-----|
| Sandbox | `https://sandbox.clicksign.com/api/v3` |
| Produção | `https://app.clicksign.com/api/v3` |

Java: `Environment.SANDBOX` / `Environment.PRODUCTION`, ou `baseUrl()` customizado no builder para testes.

## 3. Configuração

| Atributo | Tipo | Padrão | Descrição |
|----------|------|--------|-----------|
| `apiKey` | `String` | — (obrigatório) | Access token da conta |
| `environment` | enum | `PRODUCTION` | Define URL base |
| `baseUrl` | `String` | null | Sobrescreve URL do environment (WireMock, proxy) |
| `connectTimeoutMs` | `int` | `2000` | Timeout de conexão TCP |
| `readTimeoutMs` | `int` | `10000` | Timeout de leitura da resposta |
| `maxRetries` | `int` | `0` | Retentativas em erros retryable |

## 4. Formato request/response

- Content-Type e Accept: `application/vnd.api+json`
- Corpo de escrita: objeto `data` com `type`, opcional `id`, `attributes`, `relationships`
- Respostas de sucesso: `data` singular ou array; meta e `links` opcionais
- DELETE bem-sucedido: HTTP 204, corpo vazio — métodos `void` no SDK não parseiam corpo

## 5. Operações atômicas (bulk)

Request:

```json
{
  "atomic:operations": [
    { "op": "add", "data": { "type": "requirements", "attributes": {...}, "relationships": {...} } },
    { "op": "remove", "ref": { "type": "requirements", "id": "..." } }
  ]
}
```

Response:

```json
{
  "atomic:results": [
    { "data": { "id": "...", "type": "requirements", ... } },
    { "errors": [{ "detail": "...", "status": "422" }] }
  ]
}
```

Java: `BulkRequirement.Operations` + `BulkRequirement.Service.create()`.

## 6. Hierarquia de erros

| HTTP | Exceção | Retryable |
|------|---------|-----------|
| 401, 403 | Authentication | não |
| 404 | NotFound | não |
| 400, 422 | Validation | não |
| 409 | Conflict | não |
| 429 | RateLimit | sim |
| 503 | ServiceUnavailable | sim |
| ≥500 | Server | sim |
| Timeout | Timeout | sim |

Todas estendem `ClicksignException` (`RuntimeException`). Use `isRetryable()` para decisão de retry na aplicação.

Campos comuns: `statusCode()`, `requestId()` (header `x-request-id`), `responseBody()`.

## 7. Retry (HTTP principal)

- Política: **full jitter exponential backoff**
- Teto por tentativa: `ceiling = min(0.5 × 2^(attempt−1), 30)` segundos
- Espera: valor uniforme aleatório em `[0, ceiling)`
- `429` / `503`: respeita `Retry-After` quando o header está presente
- Só ocorre se `maxRetries > 0`

## 8. Paginação

- Query: `page[number]`, `page[size]`
- Filtros: `filter[chave]=valor`
- Ordenação: query `sort` (no SDK: `.order("campo")` ou `.order("-campo")`)
- Inclusão: `include=relacionamento` (no SDK: `.include("...")`)
- Auto-paginação: seguir `links.next` até esgotar; fallback por heurística de página cheia

Java: `ResourceQuery` — `.fetch()` (uma página) e `.fetchAll()` (todas as páginas). Não há `.stream()` exposto.

## 9. Bulk — retry restrito

O cliente de bulk (`BulkOperationsClient`) **só** repete em `TimeoutException` / falha de rede — não em 5xx. Motivo: operações não idempotentes em lote.

## 10. Webhook

- Algoritmo: HMAC-SHA256 do corpo bruto
- Formato esperado do header: `sha256=<hex>`
- Comparação: constant-time (via hash SHA-256 das strings)

Java:

```java
WebhookValidator.verifySignature(payload, signatureHeader, secret);
// ou
WebhookValidator.isValidSignature(payload, signatureHeader, secret);
```

## Divergências Java

| Tópico | Este SDK |
|--------|----------|
| Getters | `envelope.name()` sem prefixo `get` |
| Async | Não há cliente assíncrono nativo |
| Multi-conta | Uma instância `ClicksignClient` por token/conta |
| Exceções | Unchecked (`RuntimeException`) |

Alinhamento com Python/Ruby: [comparativo técnico](https://developers.clicksign.com/docs/comparativo-tecnico.md).
