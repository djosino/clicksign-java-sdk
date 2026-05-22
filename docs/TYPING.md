# Tipagem e DX — inventário do SDK

Padrão adotado em todas as entidades:

| Camada | Comportamento |
|--------|----------------|
| **Resposta** | Getters `String`/`Map` preservados; `*AsEnum()` / `*Typed()` fazem parse tolerante (`null` se valor desconhecido) |
| **Request** | Builders aceitam tipo forte **e** `String`/`Map` legados |
| **Enums** | `com.clicksign.resources.types.*` implementam `ApiStringEnum` |

## Pacote `com.clicksign.resources.types`

| Tipo | Uso |
|------|-----|
| `Metadata` | `metadata` em Envelope, Document |
| `EmailCustomization` | `NotificationParams` |
| `DocumentTemplate` | criar documento por modelo |
| `DocumentDuplicate` | duplicar documento |
| `AutoSignatureSigner` | bloco `signer` em termo automático |
| `EnvelopeStatus`, `DocumentStatus` | status de recursos |
| `EnvelopeLocale` | `locale` do envelope |
| `DeadlinePartialSignatureAction` | deadline parcial |
| `RequirementAction`, `RequirementRole`, `RequirementAuth` | requisitos |
| `RubricateKind` | rubrica (`initials`, `manuscript`) |
| `SignatureWatcherKind` | observadores |
| `AcceptanceTermStatus`, `SenderNameOption` | aceite WhatsApp |
| `EventCustomKind` | eventos customizados |
| `WebhookEventType` | eventos de webhook |

## Pacote `com.clicksign.resources.notarial`

| Tipo | Uso |
|------|-----|
| `CommunicateEvents` | `communicate_events` em Signer e SignatureWatcher |
| `NotificationChannel` | canais dentro de `CommunicateEvents` |

## Entidades e acessores tipados

| Entidade | Acessores tipados | Builder tipado |
|----------|-------------------|----------------|
| `Envelope` | `statusAsEnum`, `localeAsEnum`, `metadataTyped`, `deadlinePartialSignatureActionAsEnum` | locale, metadata, deadline action, status (update) |
| `Document` | `statusAsEnum`, `metadataTyped` | metadata, template, duplicate |
| `Signer` | `communicateEventsConfig` | `communicateEvents(CommunicateEvents)`, `SignatureHost` + CommunicateEvents |
| `Requirement` | `actionAsEnum`, `roleAsEnum`, `authAsEnum`, `kindAsEnum` | action, role, auth, kind |
| `SignatureWatcher` | `kindAsEnum`, `communicateEventsConfig` | kind, communicateEvents |
| `Event` | `customKindAsEnum` | `CustomParams.kind(EventCustomKind)` |
| `NotificationParams` | `emailCustomizationTyped` | emailCustomization |
| `Webhook` | `eventsAsEnums` | `addEvent(WebhookEventType)` |
| `AcceptanceTermWhatsapp` | `statusAsEnum`, `senderNameOptionAsEnum` | senderNameOption, status (update) |
| `AutoSignatureTerm` | — | `signer(AutoSignatureSigner)` |
| `BulkRequirement.Operations` | — | role, auth (use `RubricateKind.apiValue()` para kind) |

## Entidades sem enum dedicado (por design)

| Entidade | Motivo |
|----------|--------|
| `User`, `Group`, `Folder`, `Template`, `Membership` | poucos campos enumerados na API; IDs e textos livres |
| `AccessControlList`, `EnvelopeBulkCreation` | payloads pequenos ou compostos |
| `Notification` | DTO de resposta com `SummaryEntry` já tipado |

## Migração gradual

```java
// Legado (continua válido)
Signer.CreateParams.builder()
    .communicateEvents(Map.of("signature_request", "email"))
    .build();

// Tipado
Signer.CreateParams.builder()
    .communicateEvents(CommunicateEvents.builder()
        .signatureRequest(NotificationChannel.EMAIL)
        .build())
    .build();

// Leitura
CommunicateEvents config = signer.communicateEventsConfig();
```

Valores novos retornados pela API antes do SDK ser atualizado: `*AsEnum()` retorna `null`; strings brutas permanecem nos getters originais.
