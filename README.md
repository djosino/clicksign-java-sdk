# Clicksign Java SDK

![Java 11+](https://img.shields.io/badge/Java-11%2B-blue)
![CI](https://github.com/clicksign/clicksign-java-sdk/actions/workflows/ci.yml/badge.svg)
![License MIT](https://img.shields.io/badge/license-MIT-green)
[![Documentação](https://img.shields.io/badge/docs-GitHub-blue)](docs/README.md)

Cliente Java para a [Clicksign API v3](https://developers.clicksign.com/reference/comece-agora) (JSON:API, modelo Envelope). Requer **Java 11+**, **zero dependências de runtime** — usa `java.net.http.HttpClient` e parser JSON embutido.

## Índice

- [Links rápidos](#links-rápidos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Convenção de getters](#convenção-de-getters)
- [Início rápido](#início-rápido)
- [Fluxo de assinatura](#fluxo-de-assinatura)
- [Filtros, ordenação e paginação](#filtros-ordenação-e-paginação)
- [Outros recursos](#outros-recursos)
- [Recursos estendidos](#recursos-estendidos)
- [Tratamento de erros](#tratamento-de-erros)
- [Breaking changes recentes](#breaking-changes-recentes)
- [Ambientes](#ambientes)
- [Instrumentação](#instrumentação)
- [Limitações e produção](#limitações-e-produção)
- [Desenvolvimento](#desenvolvimento)
- [Licença](#licença)

## Links rápidos

| Documento | Descrição |
|-----------|-----------|
| [docs/WORKFLOW.md](docs/WORKFLOW.md) | Fluxo notarial completo |
| [docs/examples/](docs/examples/) | Cookbook (retry, bulk, webhooks, …) |
| [docs/SDK_CONTRACT.md](docs/SDK_CONTRACT.md) | Contrato multi-SDK |
| [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md) | Logs e métricas |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Problemas comuns |
| [docs/SPEC.md](docs/SPEC.md) | Mapa resource × HTTP |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Camadas internas e diagramas |
| [docs/TYPING.md](docs/TYPING.md) | Enums e tipos fortes |
| [docs/MIGRATION.md](docs/MIGRATION.md) | Notas de migração do SDK |

## Instalação

### Gradle

```kotlin
dependencies {
    implementation("br.com.josino.clicksign:clicksign-java-sdk:0.1.2")
}
```

### Maven

```xml
<dependency>
  <groupId>br.com.josino.clicksign</groupId>
  <artifactId>clicksign-java-sdk</artifactId>
  <version>0.1.2</version>
</dependency>
```

> Publicação no Maven Central conforme releases do repositório. Para build local: `./gradlew publishToMavenLocal` e use `mavenLocal()`.

## Configuração

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .maxRetries(3)
    .connectTimeoutMs(2_000)
    .readTimeoutMs(10_000)
    .onRequest(e -> { /* opcional */ })
    .build();
```

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `apiKey` | `String` | — | Access token (**obrigatório**) |
| `environment` | `Environment` | `PRODUCTION` | `SANDBOX` ou `PRODUCTION` |
| `baseUrl` | `String` | null | Sobrescreve URL (testes, proxy) |
| `connectTimeoutMs` | `int` | `2000` | Timeout de conexão (ms) |
| `readTimeoutMs` | `int` | `10000` | Timeout de leitura (ms) |
| `maxRetries` | `int` | `0` | Retentativas em erros retryable |
| `onRequest` | `Consumer<RequestEvent>` | — | Hook após cada HTTP |
| `onRetry` | `Consumer<RetryEvent>` | — | Hook antes de cada retry |
| `onError` | `Consumer<ErrorEvent>` | — | Hook em falha com exceção |

> **Segurança:** nunca commite o `apiKey`. Use variáveis de ambiente ou secret manager.

## Convenção de getters

Este SDK usa **`envelope.name()`**, não `envelope.getName()`. O mesmo vale para todos os resources (`id()`, `status()`, `metadata()`, …).

## Início rápido

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.resources.notarial.*;
import com.clicksign.resources.types.EnvelopeStatus;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .build();

Envelope envelope = client.envelopes().create(
    Envelope.CreateParams.builder().name("Contrato").locale("pt-BR").build());

Document document = client.documents().create(
    Document.CreateParams.builder()
        .envelopeId(envelope.id())
        .filename("contrato.pdf")
        .contentBase64(base64Pdf)
        .build());

Signer signer = client.signers().create(
    Signer.CreateParams.builder()
        .envelopeId(envelope.id())
        .name("João Silva")
        .email("joao@example.com")
        .build());

client.bulkRequirements().create(envelope.id(), ops -> ops
    .addAgree(signer.id(), document.id(), RequirementRole.SIGN)
    .addProvideEvidence(signer.id(), document.id(), RequirementAuth.EMAIL));

client.envelopes().update(envelope.id(),
    Envelope.UpdateParams.builder()
        .status(EnvelopeStatus.RUNNING)
        .build());
```

Token sandbox: [sandbox.clicksign.com](https://sandbox.clicksign.com).

## Fluxo de assinatura

| Etapa | Ação SDK |
|-------|----------|
| 1 | `envelopes().create(...)` |
| 2 | `documents().create(...)` |
| 3 | `signers().create(...)` |
| 4 | `requirements().create(...)` ou `bulkRequirements().create(...)` — ao menos um `agree` e um `provide_evidence` por par signatário/documento |
| 5 | `envelopes().update(id, status RUNNING)` (preferido) ou `envelopes().activate(id)` |
| 6 | `envelopes().notifyAll(...)` ou `signers().notify(...)` |
| 7 | `events().listForEnvelope(id)` |

Guia detalhado: [docs/WORKFLOW.md](docs/WORKFLOW.md).

## Filtros, ordenação e paginação

```java
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.EnvelopeStatus;

java.util.List<Envelope> page = client.envelopes().filter()
    .status(EnvelopeStatus.DRAFT)
    .name("Contrato Q1")
    .order("-created")
    .page(1)
    .perPage(20)
    .fetch();

java.util.List<Envelope> all = client.envelopes().filter()
    .status(EnvelopeStatus.DRAFT)
    .fetchAll();
```

Cookbook: [docs/examples/07-list-and-filter.md](docs/examples/07-list-and-filter.md).

## Outros recursos

| `ClicksignClient` | Resource | Operações principais |
|-------------------|----------|-------------------|
| `envelopes()` | Envelope | CRUD, `update` com `status` para ativar, `activate`, notifyAll, filter |
| `documents()` | Document | CRUD, listEvents, filter |
| `signers()` | Signer | list, retrieve, create, delete, notify, filter |
| `requirements()` | Requirement | CRUD, filter |
| `signatureWatchers()` | SignatureWatcher | list, retrieve, create, delete |
| `events()` | Event | listForEnvelope, createAddImage, createCustom |
| `bulkRequirements()` | BulkRequirement | create (atomic ops) |
| `webhooks()` | Webhook | CRUD |
| `folders()` | Folder | list, retrieve, create |
| `users()` | User | list, retrieve, me, create |
| `templates()` | Template | CRUD, listTemplateFields |
| `templateFields()` | TemplateField | list, update, delete |
| `memberships()` | Membership | CRUD, `filter()` (update via **PUT**) |
| `groups()` | Group | CRUD, addUsers, removeUsers |
| `accessControlLists()` | AccessControlList | create, destroy |
| `envelopeBulkCreations()` | EnvelopeBulkCreation | create |
| `acceptanceTermWhatsapps()` | AcceptanceTermWhatsapp | list, filter, CRUD, cancel |
| `autoSignatureTerms()` | AutoSignatureTerm | create, retrieve |

Consulta completa de rotas: [docs/SPEC.md](docs/SPEC.md).

## Recursos estendidos

**Aceite WhatsApp** (`acceptanceTermWhatsapps()`), **termo de assinatura automática** (`autoSignatureTerms()`), **criação de envelope em lote** (`envelopeBulkCreations()`) — veja [documentação da API](https://developers.clicksign.com/llms.txt) e [SPEC](docs/SPEC.md).

**Assinatura presencial:** configure `Signer.SignatureHost` em `Signer.CreateParams` — sem resource HTTP separado. Exemplo em [docs/WORKFLOW.md](docs/WORKFLOW.md).

**Tipagem:** enums em `com.clicksign.resources.types` e `CommunicateEvents` — [docs/TYPING.md](docs/TYPING.md).

## Tratamento de erros

Todas as exceções estendem `ClicksignException` (unchecked).

| HTTP | Exceção | `isRetryable()` |
|------|---------|-----------------|
| 401, 403 | `AuthenticationException` | não |
| 404 | `NotFoundException` | não |
| 400, 422 | `ValidationException` | não |
| 409 | `ConflictException` | não |
| 429 | `RateLimitException` | sim |
| 503 | `ServiceUnavailableException` | sim |
| 5xx | `ServerException` | sim |
| Timeout | `TimeoutException` | sim |

```java
import com.clicksign.errors.*;

try {
    client.envelopes().retrieve("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
} catch (NotFoundException e) {
    System.err.println(e.requestId());   // x-request-id da API
    System.err.println(e.responseBody());
} catch (RateLimitException e) {
    if (e.isRetryable()) { /* SDK já esgotou maxRetries */ }
} catch (ClicksignException e) {
    System.err.println(e.getMessage());
}
```

Mais casos: [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md).

## Ambientes

| `Environment` | URL base |
|---------------|----------|
| `SANDBOX` | `https://sandbox.clicksign.com/api/v3` |
| `PRODUCTION` | `https://app.clicksign.com/api/v3` |

Documentação: [Informações gerais](https://developers.clicksign.com/docs/informacoes-gerais.md) · [Migração 1.9 → 3.0](https://developers.clicksign.com/docs/guia-de-migracao.md)

## Instrumentação

Configure hooks no **builder** (não após `build()`):

```java
ClicksignClient client = ClicksignClient.builder()
    .apiKey(apiKey)
    .onRequest(e -> log.info("{} {} → {}", e.method(), e.path(), e.status()))
    .onRetry(e -> log.warn("retry {} wait {}ms", e.attempt(), e.waitMs()))
    .onError(e -> log.error("erro {}", e.error().toString()))
    .build();
```

Detalhes: [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md).

## Limitações e produção

| Tópico | Detalhe |
|--------|---------|
| Async | **Não há** `AsyncClicksignClient` — use `ExecutorService` na aplicação |
| HTTP | `java.net.http.HttpClient` por instância; sem pool configurável na API pública |
| Thread safety | `ClicksignClient` imutável — reutilize entre threads |
| Bulk retry | Apenas timeout, não 5xx ([docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)) |

[docs/examples/08-production-limitations.md](docs/examples/08-production-limitations.md)

## Breaking changes recentes

| Área | Mudança |
|------|---------|
| Metadata | `Map<String, Object>` em envelope/documento |
| Signatário | `SignatureHost`; `NotificationParams` compartilhado |
| Folder | Sem `update`/`delete` na API 3.0 |
| Requisitos | `list(envelopeId)` unificado |

Detalhes: [CHANGELOG.md](CHANGELOG.md) · [docs/MIGRATION.md](docs/MIGRATION.md).

## Desenvolvimento

```bash
./gradlew test
./gradlew checkstyleMain checkstyleTest
./gradlew build
./gradlew jar
```

Teste opcional contra sandbox:

```bash
CLICKSIGN_API_KEY=... ./gradlew test -PincludeIntegration --tests "*.SandboxIntegrationTest"
```

## Licença

MIT — veja [LICENSE](LICENSE).
