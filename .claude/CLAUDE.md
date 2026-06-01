# clicksign-java-sdk

## Comandos

```bash
./gradlew test                                              # todos os testes
./gradlew test --tests "com.clicksign.SomeTest"            # classe específica
./gradlew test --tests "com.clicksign.SomeTest.method"     # método específico
./gradlew checkstyleMain checkstyleTest                    # lint
./gradlew jar                                              # build JAR
./gradlew build                                            # compile + test + lint
```

## Localização dos arquivos-chave

### HTTP e core
- Entry point: `src/main/java/com/clicksign/ClicksignClient.java` — builder pattern, expõe todos os `Service`s
- Configuração: `src/main/java/com/clicksign/ClientConfig.java` — apiKey, baseUrl, timeouts, maxRetries
- HTTP client: `src/main/java/com/clicksign/http/HttpClient.java` — retry full jitter, get/post/patch/put/delete
- Extração de erros: `src/main/java/com/clicksign/http/ErrorMessageExtractor.java` — status → exceção
- Bulk HTTP: `src/main/java/com/clicksign/jsonapi/BulkOperationsClient.java` — retry só TimeoutException (ops atômicas não são idempotentes)

### JSON:API
- Parser: `src/main/java/com/clicksign/jsonapi/JsonApiParser.java` — `ResourceObject` com attributes/relationships
- Serializer: `src/main/java/com/clicksign/jsonapi/JsonApiSerializer.java` — monta request body
- JSON puro: `src/main/java/com/clicksign/jsonapi/MinimalJsonParser.java` — recursive-descent, sem deps externas
- Query builder: `src/main/java/com/clicksign/jsonapi/ResourceQuery.java` — filter/order/page/include/fields/fetchAll
- Atomic ops: `src/main/java/com/clicksign/jsonapi/AtomicOperations.java` — builder para JSON:API atomic operations

### Erros
- Base: `src/main/java/com/clicksign/errors/ClicksignException.java`
- `AuthenticationException` (401/403), `NotFoundException` (404), `ValidationException` (400/422)
- `ConflictException` (409), `RateLimitException` (429), `ServiceUnavailableException` (503), `ServerException` (5xx)
- `TimeoutException`, `WebhookSignatureException`
- Retryable: `RateLimitException`, `ServiceUnavailableException`, `ServerException`, `TimeoutException`
- Campos comuns: `statusCode()`, `requestId()` (header `x-request-id`), `responseBody()`
- `RateLimitException` expõe `retryAfterSeconds()` — valor do header `Retry-After`

### Instrumentação
- Registry: `src/main/java/com/clicksign/instrumentation/Instrumentation.java` — CopyOnWriteArrayList thread-safe
- Hooks configurados **só no builder** — não existe `client.instrumentation()` após `build()`
- Eventos: `RequestEvent` (method, path, status, attempt, durationMs), `RetryEvent` (attempt, maxRetries, waitMs, error), `ErrorEvent` (method, path, status=0 em timeout, durationMs, error)
- Ordem: `onRequest` → `onError` (se ≥400) → `onRetry` (se retryable e restantes)
- `BulkOperationsClient` **não** publica eventos de Instrumentation

### Webhook
- Validação HMAC-SHA256: `src/main/java/com/clicksign/webhook/WebhookValidator.java` — constant-time comparison
- Usar corpo **bruto** da requisição — nunca re-serializar JSON

### Resources
- `src/main/java/com/clicksign/resources/notarial/` — Envelope, Document, Signer, Requirement, BulkRequirement, SignatureWatcher, Event
- `src/main/java/com/clicksign/resources/` — Webhook, Folder, User, Template, TemplateField, Membership, Group, AccessControlList, EnvelopeBulkCreation
- `src/main/java/com/clicksign/resources/types/` — enums e value objects (ver seção Tipagem)

### Testes
- Fixtures JSON:API: `src/test/java/com/clicksign/JsonApiFixtures.java`
- WireMock para stub HTTP, JUnit 5, Mockito

## Padrões estabelecidos

### Resource class
Cada resource é uma classe `final` com:
- Campos `private final` + getters sem prefixo `get` — `envelope.name()`, nunca `envelope.getName()`
- `Service` como classe interna estática — recebe `HttpClient` por construtor
- `CreateParams` e `UpdateParams` como classes internas com Builder
- `IllegalArgumentException` para campos obrigatórios ausentes no `build()`
- Construtor privado que recebe `JsonApiParser.ResourceObject`

### Nomes de método no Service
- `list()` / `list(parentId)` — GET collection
- `retrieve(id)` / `retrieve(id, parentId)` — GET single
- `create(params)` — POST
- `update(id, params)` — PATCH (exceto Membership: PUT)
- `delete(id)` / `delete(id, parentId)` — DELETE

### Imutabilidade
- `Map.copyOf` / `Collections.unmodifiableMap` em `ResourceObject`
- Params e entidades imutáveis após construção

### Retry
- Full jitter exponential backoff: `ceiling = min(0.5 × 2^(attempt-1), 30)` segundos, espera uniforme `[0, ceiling)`
- 429/503: respeita `Retry-After` quando presente
- Só ocorre se `maxRetries > 0` (padrão = 0)
- `BulkOperationsClient`: retry só em `TimeoutException`, nunca em 5xx

### Convenções de teste
- UUIDs: `"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"` — nunca UUIDs reais de sandbox
- WireMock na porta 8089; `JsonApiFixtures.BASE_URL = "http://localhost:8089"`
- Stubs de erro: `JsonApiFixtures.errorBody("mensagem")`
- Todo resource test tem bloco verificando endpoint e resource type

## Arquitetura de autorização
- Header: `Authorization: <token>` — **SEM** prefixo `Bearer`
- Sandbox: `https://sandbox.clicksign.com/api/v3` / Produção: `https://app.clicksign.com/api/v3`
- `ClicksignClient` imutável após `build()`, thread-safe; uma instância por token/conta

## Versão
Lida de `REVISION` em tempo de compilação via `build.gradle.kts`.

## Tipagem (`com.clicksign.resources.types`)

| Tipo | Uso |
|------|-----|
| `EnvelopeStatus` | status do envelope (`RUNNING`, `DRAFT`, …) |
| `EnvelopeLocale` | `locale` do envelope (`PT_BR`, …) |
| `DocumentStatus` | status do documento |
| `RequirementAction` | `AGREE`, `PROVIDE_EVIDENCE` |
| `RequirementRole` | `SIGN`, … |
| `RequirementAuth` | `EMAIL`, … |
| `RubricateKind` | `initials`, `manuscript` — usar `.apiValue()` em BulkRequirement |
| `SignatureWatcherKind` | tipo de observador |
| `WebhookEventType` | eventos de webhook |
| `MembershipRole` | `admin`, `member` |
| `AcceptanceTermStatus`, `SenderNameOption` | aceite WhatsApp |
| `EventCustomKind` | eventos customizados |
| `DeadlinePartialSignatureAction` | deadline parcial |
| `Metadata` | `metadata` em Envelope e Document |
| `EmailCustomization` | `NotificationParams` |
| `DocumentTemplate` | criar doc por modelo |
| `DocumentDuplicate` | duplicar documento |
| `AutoSignatureSigner` | bloco `signer` em termo automático |

Padrão de acesso: `*AsEnum()` retorna `null` (não lança) para valores desconhecidos; string bruta preservada no getter original.

Queries tipadas: `EnvelopeQuery`, `DocumentQuery`, `SignerQuery`, `RequirementQuery`, `AcceptanceTermWhatsappQuery`, `MembershipQuery` — todas em `com.clicksign.jsonapi`.

## Mapa de endpoints (SPEC)

> Referência completa: `docs/SPEC.md`

### Núcleo notarial

| Resource | Método | HTTP | Path |
|----------|--------|------|------|
| Envelope | `list()` | GET | `/envelopes` |
| Envelope | `retrieve(id)` | GET | `/envelopes/{id}` |
| Envelope | `create(params)` | POST | `/envelopes` |
| Envelope | `update(id, params)` | PATCH | `/envelopes/{id}` |
| Envelope | `delete(id)` | DELETE | `/envelopes/{id}` |
| Envelope | `activate(id)` | POST | `/envelopes/{id}/activate` |
| Envelope | `notifyAll(id, params)` | POST | `/envelopes/{id}/notifications` |
| Document | `list(eid)` | GET | `/envelopes/{eid}/documents` |
| Document | `retrieve(id, eid)` | GET | `/envelopes/{eid}/documents/{id}` |
| Document | `create(params)` | POST | `/envelopes/{eid}/documents` |
| Document | `update(id, eid, params)` | PATCH | `/envelopes/{eid}/documents/{id}` |
| Document | `delete(id, eid)` | DELETE | `/envelopes/{eid}/documents/{id}` |
| Document | `listEvents(did, eid)` | GET | `/envelopes/{eid}/documents/{id}/events` |
| Signer | `list(eid)` | GET | `/envelopes/{eid}/signers` |
| Signer | `retrieve(id, eid)` | GET | `/envelopes/{eid}/signers/{id}` |
| Signer | `create(params)` | POST | `/envelopes/{eid}/signers` |
| Signer | `delete(id, eid)` | DELETE | `/envelopes/{eid}/signers/{id}` |
| Signer | `notify(id, eid, params)` | POST | `/envelopes/{eid}/signers/{id}/notifications` |
| Requirement | `list(eid)` | GET | `/envelopes/{eid}/requirements` |
| Requirement | `retrieve(id, eid)` | GET | `/envelopes/{eid}/requirements/{id}` |
| Requirement | `create(params)` | POST | `/envelopes/{eid}/requirements` |
| Requirement | `delete(id, eid)` | DELETE | `/envelopes/{eid}/requirements/{id}` |
| SignatureWatcher | `list(eid)` | GET | `/envelopes/{eid}/signature_watchers` |
| SignatureWatcher | `retrieve(id, eid)` | GET | `/envelopes/{eid}/signature_watchers/{id}` |
| SignatureWatcher | `create(params)` | POST | `/envelopes/{eid}/signature_watchers` |
| SignatureWatcher | `delete(id, eid)` | DELETE | `/envelopes/{eid}/signature_watchers/{id}` |
| Event | `listForEnvelope(eid)` | GET | `/envelopes/{eid}/events` |
| Event | `create(params)` | POST | `/envelopes/{eid}/documents/{did}/events` |
| BulkRequirement | `create(eid, builder)` | POST | `/envelopes/{eid}/bulk_requirements` |

### Conta e organização

| Resource | Método | HTTP | Path | Notas |
|----------|--------|------|------|-------|
| Webhook | CRUD | GET/POST/PATCH/DELETE | `/webhooks[/{id}]` | |
| Folder | `list`, `retrieve`, `create` | GET/POST | `/folders[/{id}]` | sem update/delete |
| User | `list`, `retrieve`, `me`, `create` | GET/POST | `/users[/{id}]` | |
| Template | CRUD + `listTemplateFields` | GET/POST/PATCH/DELETE | `/templates[/{id}]` | |
| TemplateField | `update(id, tid)`, `delete(id, tid)` | PATCH/DELETE | `/templates/{tid}/template_fields/{id}` | |
| Membership | CRUD | GET/**PUT**/DELETE | `/memberships[/{id}]` | update = PUT |
| Group | CRUD + `addUsers`, `removeUsers` | GET/POST/PATCH/DELETE | `/groups[/{id}]` | |
| Group | `addUsers(gid, ids)` | POST | `/groups/{id}/relationships/users` | |
| Group | `removeUsers(gid, ids)` | DELETE | `/groups/{id}/relationships/users` | body JSON |
| AccessControlList | `create(fid, gid)` | POST | `/access_control_lists` | só relationships |
| AccessControlList | `destroy(fid, gid)` | DELETE | `/access_control_lists` | body JSON |
| EnvelopeBulkCreation | `create(params)` | POST | `/envelope_bulk_creations` | job assíncrono |
| AcceptanceTermWhatsapp | CRUD + `cancel` | GET/POST/PATCH | `/acceptance_term/whatsapps[/{id}]` | |
| AutoSignatureTerm | `retrieve`, `create` | GET/POST | `/auto_signature/terms[/{id}]` | |

## Paginação

- Query params: `page[number]`, `page[size]`; filtros: `filter[chave]=valor`; ordem: `sort`
- `.fetch()` — uma página; `.fetchAll()` — todas (segue `links.next`)
- `ResourceQuery` genérico disponível em todas as subclasses via herança

## Backlog (docs/TODO.md)

- [ ] Avaliar `TemplateField.list()` standalone — `GET /template_fields` não documentado na v3; só existe `GET /templates/{id}/template_fields`

## Boas práticas (não repetir erros)

- `BulkOperationsClient` retria só `TimeoutException`, não `ServerException` — ops atômicas não são idempotentes
- `WebhookValidator.isValidSignature` usa comparação constant-time — nunca comparação direta de string
- `Folder` não tem update/delete — API não documenta esses endpoints
- `Membership.update` usa `http.put()`, não `http.patch()` — API usa PUT
- `Requirement.list` usa `GET /envelopes/{id}/requirements` — paths `/documents/{id}/relationships/requirements` não existem na v3
- `Requirement` não tem `update` — `PATCH /envelopes/{id}/requirements/{id}` não existe na API
- `communicate_events` é `Map<String, Object>` — nunca `String`
- `signature_host` é objeto (`SignatureHost` inner class) — nunca `String`
- `metadata` em Envelope e Document é `Map<String, Object>` — nunca `String`
- Ativar envelope exige **pelo menos um requisito `agree`** (com `role`) + **um `provide_evidence`** (com `auth`) por par signatário/documento
- `BulkRequirement.Response`: checar `isSuccess()` e iterar `failures()` — erros por slot não lançam exceção global
- `statusAsEnum()` retorna `null` para valores novos da API — nunca presumir não-null sem verificar
- `TemplateField.update/delete` usa path `/templates/{templateId}/template_fields/{id}` — não `/template_fields/{id}`

## Referências

- `docs/SPEC.md` — mapa completo de endpoints com notas
- `docs/TYPING.md` — inventário de enums e acessores tipados
- `docs/SDK_CONTRACT.md` — contrato multi-SDK (Python/Ruby/Java)
- `docs/ARCHITECTURE.md` — fluxo HTTP, bulk, thread safety
- `docs/WORKFLOW.md` — fluxo ponta-a-ponta com exemplos de código
- `docs/OBSERVABILITY.md` — hooks de instrumentação, SLF4J, Micrometer
- `docs/TROUBLESHOOTING.md` — sintomas comuns e correções

## Skills (slash commands)
- `/gen-resource <name>` — gera novo resource + testes
- `/sync-spec` — compara rotas da API com resources do SDK, lista gaps
- `/release` — checklist de release
- `/run` — smoke test via `run` skill
