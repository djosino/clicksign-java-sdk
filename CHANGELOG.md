# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

## [0.1.3]

### Removed

- `Requirement.Service#update()` and `Requirement.UpdateParams` — the API does not expose `PATCH /envelopes/{id}/requirements/{id}`; the method was a spec error

### Changed

- `docs/SPEC.md` — removed Requirement update row; clarified Membership uses PUT (not PATCH)

## [0.1.2]

### Changed

- Javadoc completo em todos os tipos e membros públicos — zero warnings com Java 21 (`-Xdoclint:all`)
- Construtores de exceção documentados com `@param` em todas as subclasses de `ClicksignException`
- Enums tipados (`EnvelopeStatus`, `DocumentStatus`, `RequirementAction`, `RequirementAuth`, `RequirementRole`, `MembershipRole`, `NotificationChannel`, `WebhookEventType`, entre outros) com comentário em cada constante
- Builders documentados: `@param`/`@return` em todos os setters, `build()` e `builder()`
- Services documentados: construtores, `list`, `retrieve`, `create`, `update`, `delete`, `filter` em todos os resources
- Tipos auxiliares (`Metadata`, `CommunicateEvents`, `EmailCustomization`, `DocumentTemplate`, `DocumentDuplicate`, `SignatureHost`) com Javadoc completo
- Camada HTTP/infraestrutura (`HttpClient`, `Instrumentation`, `ErrorMessageExtractor`, `JsonApiParser`, `JsonApiSerializer`, `ResourceQuery`, `TypedResourceQuery`) com Javadoc nos métodos públicos

### Added

- `AcceptanceTermWhatsapp` — list, filter, retrieve, create, cancel (WhatsApp acceptance / Click.Agree)
- `AutoSignatureTerm` — create automatic signature authorization terms
- `CommunicateEvents` and `NotificationChannel` — typed `communicate_events` (maps still supported)
- `com.clicksign.resources.types` — enums and value types (`Metadata`, `DocumentTemplate`, `RequirementRole`, `EnvelopeStatus`, …)
- Typed accessors on resources (`statusAsEnum()`, `metadataTyped()`, `communicateEventsConfig()`, …)
- `ClicksignClient.acceptanceTermWhatsapps()` and `autoSignatureTerms()`
- README, examples, migration notes, and [docs/TYPING.md](docs/TYPING.md)
- Public documentation set: [docs/README.md](docs/README.md), WORKFLOW, ARCHITECTURE, OBSERVABILITY, TROUBLESHOOTING, SPEC, SDK_CONTRACT, examples/
- Compilable `examples/` Gradle module mirroring cookbook recipes
- `AutoSignatureTerm.retrieve(id)` — GET `/auto_signature/terms/{id}`
- JaCoCo minimum 70% coverage for `com.clicksign.resources` and `com.clicksign.resources.types`
- Typed list queries: `EnvelopeQuery`, `DocumentQuery`, `SignerQuery`, `RequirementQuery`, `AcceptanceTermWhatsappQuery`, `MembershipQuery`
- `MembershipRole`, `Membership.filter()`, `Membership.roleAsEnum()`

### Changed

- Request builders across notarial resources accept typed enums and value objects alongside `String`/`Map`
- `Signer.CreateParams` and `SignatureWatcher.CreateParams` accept `CommunicateEvents` in addition to `Map`

## [0.1.0]

Initial release aligned with Clicksign API 3.0 (Envelope).

### Breaking changes vs. informal pre-releases

- `metadata` on envelopes and documents is `Map<String, Object>` (not `String`)
- `Signer.SignatureHost` replaces loose maps for in-person signature hosts
- `Signer.NotifyParams` removed — use shared `NotificationParams`
- `Folder` no longer exposes `update` or `delete` (API 3.0)
- `Requirement.listForDocument` / `listForSigner` removed — use `list(envelopeId)` or `filter()`
