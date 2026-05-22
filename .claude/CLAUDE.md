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
- `ConflictException` (409), `RateLimitException` (429), `ServerException` (5xx)
- `TimeoutException`, `WebhookSignatureException`
- `RateLimitException`, `ServerException`, `TimeoutException` → `isRetryable() = true`

### Instrumentação
- Registry: `src/main/java/com/clicksign/instrumentation/Instrumentation.java` — CopyOnWriteArrayList thread-safe
- Eventos: `RequestEvent`, `RetryEvent`, `ErrorEvent`

### Webhook
- Validação HMAC-SHA256: `src/main/java/com/clicksign/webhook/WebhookValidator.java` — constant-time comparison

### Resources
- `src/main/java/com/clicksign/resources/notarial/` — Envelope, Document, Signer, Requirement, BulkRequirement, SignatureWatcher, Event
- `src/main/java/com/clicksign/resources/` — Webhook, Folder, User, Template, TemplateField, Membership, Group, AccessControlList, EnvelopeBulkCreation

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
Full jitter exponential backoff: `Math.min(0.5 × 2^(attempt-1), 30)` segundos, intervalo uniforme `[0, ceiling)`.

### Convenções de teste
- UUIDs: `"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"` — nunca UUIDs reais de sandbox
- WireMock na porta 8089; `JsonApiFixtures.BASE_URL = "http://localhost:8089"`
- Stubs de erro: `JsonApiFixtures.errorBody("mensagem")`
- Todo resource test tem bloco verificando endpoint e resource type

## Arquitetura de autorização
- Header: `Authorization: <token>` — **SEM** prefixo `Bearer`

## Versão
Lida de `REVISION` em tempo de compilação via `build.gradle.kts`.

## Contrato comportamental
Leia `docs/TODO.md` antes de implementar qualquer resource — lista campos pendentes, operações e gaps vs API.

## Boas práticas (não repetir erros)
- `BulkOperationsClient` retria só `TimeoutException`, não `ServerException` — ops atômicas não são idempotentes
- `WebhookValidator.isValidSignature` usa `MessageDigest` + `hmac.compare_digest` equivalente — nunca comparação direta de string
- `Folder` não tem update/delete — API não documenta esses endpoints
- `Membership.update` usa `http.put()`, não `http.patch()` — API usa PUT
- `Requirement.list` usa `GET /envelopes/{id}/requirements` — paths `/documents/{id}/relationships/requirements` não existem na v3
- `communicate_events` é `Map<String, Object>` — nunca `String`
- `signature_host` é objeto (`SignatureHost` inner class) — nunca `String`
- `metadata` em Envelope e Document é `Map<String, Object>` — nunca `String`

## Skills (slash commands)
- `/gen-resource <name>` — gera novo resource + testes
- `/sync-spec` — compara rotas da API com resources do SDK, lista gaps
- `/release` — checklist de release
- `/run` — smoke test via `run` skill
