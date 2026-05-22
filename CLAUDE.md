# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew test                          # all tests
./gradlew test --tests "com.clicksign.SomeTest"          # single class
./gradlew test --tests "com.clicksign.SomeTest.method"   # single method
./gradlew checkstyleMain checkstyleTest # lint
./gradlew jar                           # build JAR
./gradlew build                         # compile + test + lint
```

## Architecture

Java 11, Gradle Kotlin DSL, **zero runtime dependencies** — só `java.net.http.HttpClient` (Java 11+) e stdlib. JSON parsing feito por `MinimalJsonParser` (recursive-descent, sem Gson/Jackson).

### Camadas

```
ClicksignClient                         entry point — builder pattern
  └── ClientConfig                      configuração imutável
  └── HttpClient                        HTTP, retry, headers, erro → exception
       └── ErrorMessageExtractor        extrai detail/title do JSON:API error
  └── Resource.Service (por resource)   CRUD: list/retrieve/create/update/delete
       └── JsonApiParser                parse responses → ResourceObject
       └── JsonApiSerializer            monta request body JSON:API
```

### Padrões Java estabelecidos neste SDK

**Entry point:** `ClicksignClient.builder().apiKey(...).environment(...).build()` — Builder pattern, `IllegalStateException` se apiKey ausente.

**Resources:** cada resource é uma classe final com:
- Campos privados + getters sem prefixo `get` (ex: `envelope.name()`, não `envelope.getName()`)
- `Service` como classe interna estática — instanciada e exposta por `ClicksignClient`
- `CreateParams` e `UpdateParams` como classes internas com Builder — `IllegalArgumentException` para campos obrigatórios ausentes
- Construtor privado que recebe `JsonApiParser.ResourceObject`

**Exceções:** todas estendem `ClicksignException` (unchecked). Hierarquia:
- `AuthenticationException` (401, 403)
- `NotFoundException` (404)
- `ValidationException` (400, 422)
- `ConflictException` (409)
- `RateLimitException` (429) — `isRetryable() = true`
- `ServerException` (5xx) — `isRetryable() = true`
- `TimeoutException` — `isRetryable() = true`
- `WebhookSignatureException`

**Retry:** full jitter exponential backoff — `Math.min(0.5 × 2^(attempt-1), 30)` segundos, intervalo uniforme em `[0, ceiling)`.

**Imutabilidade:** recursos e params são imutáveis após construção. `Map.copyOf` / `Collections.unmodifiableMap` em `JsonApiParser.ResourceObject`.

**Nomes de métodos:** sem prefixo `get` — `id()`, `name()`, `status()`. Padrão moderno Java (Project Lombok faz o mesmo, mas sem dependência).

**Encapsulamento do Service:** `Service` recebe `HttpClient` por construtor (não acopla a `ClicksignClient` diretamente) — facilita testes com mock.

### JSON sem dependências

`MinimalJsonParser` — recursive-descent completo: objetos, arrays, strings (com escapes Unicode), números, booleans, null. Suficiente para JSON:API. Não usar para parsing genérico fora do SDK.

`JsonApiSerializer` — monta JSON:API bodies para requests. Suporta `Map<String, Object>` aninhado, arrays, primitivos.

### Versão

Lida de `REVISION` em tempo de compilação via `build.gradle.kts`:
```kotlin
version = Files.readString(rootProject.file("REVISION").toPath()).trim()
```

### Release

Push em `release/*` → CI (Java 11/17/21) → publish Maven Central + tag `vX.Y.Z`.
Secrets necessários: `MAVEN_USERNAME`, `MAVEN_PASSWORD`, `SIGNING_KEY`, `SIGNING_PASSWORD`.

## Estrutura de diretórios

```
src/main/java/com/clicksign/
  ClicksignClient.java          entry point
  ClientConfig.java             configuração imutável
  Environment.java              enum PRODUCTION / SANDBOX
  errors/                       hierarquia de exceções
  http/
    HttpClient.java             java.net.http, retry, error handling
    ErrorMessageExtractor.java  extrai mensagem do JSON:API error body
  jsonapi/
    JsonApiParser.java          parse de responses
    JsonApiSerializer.java      serialização de requests
    MinimalJsonParser.java      recursive-descent JSON parser
  resources/
    notarial/                   Envelope, Document, Signer, Requirement, ...
    Webhook.java
    Folder.java
    User.java
    ...
```
