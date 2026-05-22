# Arquitetura do SDK

Visão das camadas internas do Clicksign Java SDK e como uma chamada percorre o código até a API v3.

## Fluxo HTTP padrão

```mermaid
flowchart TB
  subgraph app [Aplicação Java]
    BLD[ClicksignClient.builder]
    SVC[Envelope.Service / Document.Service / ...]
  end

  subgraph sdk [SDK]
    HC[HttpClient]
    RB[retry full-jitter]
    INS[Instrumentation]
    EH[ErrorMessageExtractor]
    PARSER[JsonApiParser]
    SERIAL[JsonApiSerializer]
  end

  API[(Clicksign API v3)]

  BLD --> HC
  SVC --> HC
  HC --> INS
  HC --> EH
  HC --> API
  INS -.-> RB
  SVC --> PARSER
  SVC --> SERIAL
```

### Camadas

| Camada | Responsabilidade |
|--------|------------------|
| `ClicksignClient` | Ponto de entrada; expõe `Service` por resource |
| `*.Service` | CRUD e ações de domínio; monta path e body |
| `HttpClient` | `java.net.http.HttpClient`, headers JSON:API, retry, exceções |
| `JsonApiSerializer` | Request bodies `data.type` + `attributes` + `relationships` |
| `JsonApiParser` | Response → `ResourceObject` → classes de domínio |
| `MinimalJsonParser` | Parser JSON recursive-descent (zero dependências) |

## Operações em massa (bulk)

```mermaid
flowchart LR
  BR[BulkRequirement.Service]
  OPS[Operations builder]
  AO[AtomicOperations]
  BOC[BulkOperationsClient]
  API[(POST /envelopes/id/bulk_requirements)]

  BR --> OPS
  OPS --> AO
  AO --> BOC
  BOC --> API
  API --> RSP[Response + atomic:results]
```

`BulkOperationsClient` difere do `HttpClient` principal:

| Comportamento | `HttpClient` | `BulkOperationsClient` |
|---------------|--------------|------------------------|
| Retry em 5xx / 429 | Sim (se `maxRetries > 0`) | Não |
| Retry em timeout | Sim | Sim |
| Corpo em 4xx/5xx com `atomic:results` | Lança exceção | Retorna corpo para parse parcial |

Motivo: operações atômicas podem falhar parcialmente por slot; reenviar o lote inteiro após 500 pode duplicar requisitos já criados.

## Instrumentação no fluxo HTTP

```mermaid
sequenceDiagram
  participant App
  participant HC as HttpClient
  participant API

  App->>HC: envelopes().retrieve()
  HC->>API: GET /envelopes/id
  API-->>HC: 404
  HC->>App: onRequest(404)
  HC->>App: onError(NotFoundException)
  Note over App: exceção propagada
```

## Retry (HTTP principal)

Com `maxRetries > 0`:

1. Calcula teto: `ceiling = min(0.5 × 2^(attempt−1), 30)` segundos  
2. Espera aleatória uniforme em `[0, ceiling)` (full jitter), **ou** espera `Retry-After` em 429/503 quando o header vier preenchido  
3. Publica `onRetry` e repete em `RateLimitException`, `ServerException`, `ServiceUnavailableException`, `TimeoutException`

## Cliente HTTP e thread safety

- Uma instância de `java.net.http.HttpClient` por `ClicksignClient` (criada no construtor de `HttpClient` / `BulkOperationsClient`)
- Sem connection pool configurável na API pública — a JVM reutiliza conexões internamente
- `ClicksignClient` é imutável após `build()` e seguro para uso concorrente entre threads (desde que params/builders não sejam compartilhados mutáveis)

## O que não está no SDK

| Recurso | Status |
|---------|--------|
| Cliente assíncrono | Não implementado — use threads ou `CompletableFuture` na aplicação |
| Envio de header `X-Request-Id` customizado | Não exposto no builder — use `requestId()` da resposta de erro para correlação com suporte |
| Gson / Jackson | Não usados — parser próprio |

## Referências

- [SDK_CONTRACT.md](SDK_CONTRACT.md) — contrato multi-SDK  
- [OBSERVABILITY.md](OBSERVABILITY.md) — hooks de diagnóstico  
