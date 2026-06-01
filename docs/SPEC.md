# Especificação de endpoints (SDK Java)

Mapa dos métodos públicos do SDK para rotas HTTP da API 3.0. Paths são relativos à base (`/api/v3` já incluída no `Environment`).

Legenda: **Q** = `ResourceQuery` via `.filter()`.

## Núcleo notarial

| Resource | Método SDK | HTTP | Path | Notas |
|----------|------------|------|------|-------|
| `Envelope` | `list()` | GET | `/envelopes` | |
| `Envelope` | `filter()` | GET | `/envelopes` | `EnvelopeQuery`: status, name, created, modified, deadline_at |
| `Envelope` | `retrieve(id)` | GET | `/envelopes/{id}` | |
| `Envelope` | `create(params)` | POST | `/envelopes` | |
| `Envelope` | `update(id, params)` | PATCH | `/envelopes/{id}` | `status: running` para ativar (preferido); exige requisitos `agree` + `provide_evidence` |
| `Envelope` | `delete(id)` | DELETE | `/envelopes/{id}` | |
| `Envelope` | `activate(id)` | POST | `/envelopes/{id}/activate` | Alternativa a `update` com `running`; mesmos pré-requisitos de requisitos |
| `Envelope` | `notifyAll(envelopeId, params)` | POST | `/envelopes/{id}/notifications` | |
| `Document` | `list(envelopeId)` | GET | `/envelopes/{eid}/documents` | |
| `Document` | `filter(envelopeId)` | GET | `/envelopes/{eid}/documents` | Q |
| `Document` | `retrieve(id, envelopeId)` | GET | `/envelopes/{eid}/documents/{id}` | |
| `Document` | `create(params)` | POST | `/envelopes/{eid}/documents` | |
| `Document` | `update(id, envelopeId, params)` | PATCH | `/envelopes/{eid}/documents/{id}` | |
| `Document` | `delete(id, envelopeId)` | DELETE | `/envelopes/{eid}/documents/{id}` | |
| `Document` | `listEvents(docId, envelopeId)` | GET | `/envelopes/{eid}/documents/{id}/events` | |
| `Signer` | `list(envelopeId)` | GET | `/envelopes/{eid}/signers` | |
| `Signer` | `filter(envelopeId)` | GET | `/envelopes/{eid}/signers` | Q |
| `Signer` | `retrieve(id, envelopeId)` | GET | `/envelopes/{eid}/signers/{id}` | |
| `Signer` | `create(params)` | POST | `/envelopes/{eid}/signers` | Sem update na API |
| `Signer` | `delete(id, envelopeId)` | DELETE | `/envelopes/{eid}/signers/{id}` | |
| `Signer` | `notify(id, envelopeId, params)` | POST | `/envelopes/{eid}/signers/{id}/notifications` | void |
| `Requirement` | `list(envelopeId)` | GET | `/envelopes/{eid}/requirements` | |
| `Requirement` | `filter(envelopeId)` | GET | `/envelopes/{eid}/requirements` | Q |
| `Requirement` | `retrieve(id, envelopeId)` | GET | `/envelopes/{eid}/requirements/{id}` | |
| `Requirement` | `create(params)` | POST | `/envelopes/{eid}/requirements` | relationships no body |
| `Requirement` | `delete(id, envelopeId)` | DELETE | `/envelopes/{eid}/requirements/{id}` | |
| `SignatureWatcher` | `list(envelopeId)` | GET | `/envelopes/{eid}/signature_watchers` | |
| `SignatureWatcher` | `retrieve(id, envelopeId)` | GET | `/envelopes/{eid}/signature_watchers/{id}` | |
| `SignatureWatcher` | `create(params)` | POST | `/envelopes/{eid}/signature_watchers` | |
| `SignatureWatcher` | `delete(id, envelopeId)` | DELETE | `/envelopes/{eid}/signature_watchers/{id}` | |
| `Event` | `listForEnvelope(envelopeId)` | GET | `/envelopes/{eid}/events` | |
| `Event` | `create(params)` | POST | `/envelopes/{eid}/documents/{did}/events` | |
| `Event` | `createAddImage(params)` | POST | `/envelopes/{eid}/documents/{did}/events` | |
| `Event` | `createCustom(params)` | POST | `/envelopes/{eid}/documents/{did}/events` | |
| `BulkRequirement` | `create(envelopeId, builder)` | POST | `/envelopes/{eid}/bulk_requirements` | `atomic:operations` |

## Conta e organização

| Resource | Método SDK | HTTP | Path | Notas |
|----------|------------|------|------|-------|
| `Webhook` | `list()` | GET | `/webhooks` | |
| `Webhook` | `retrieve(id)` | GET | `/webhooks/{id}` | |
| `Webhook` | `create(params)` | POST | `/webhooks` | |
| `Webhook` | `update(id, params)` | PATCH | `/webhooks/{id}` | |
| `Webhook` | `delete(id)` | DELETE | `/webhooks/{id}` | |
| `Folder` | `list()` | GET | `/folders` | Sem update/delete |
| `Folder` | `retrieve(id)` | GET | `/folders/{id}` | |
| `Folder` | `create(params)` | POST | `/folders` | |
| `User` | `list()` | GET | `/users` | |
| `User` | `retrieve(id)` | GET | `/users/{id}` | |
| `User` | `me()` | GET | `/users/me` | |
| `User` | `create(params)` | POST | `/users` | |
| `Template` | `list()` | GET | `/templates` | |
| `Template` | `retrieve(id)` | GET | `/templates/{id}` | |
| `Template` | `create(params)` | POST | `/templates` | |
| `Template` | `update(id, params)` | PATCH | `/templates/{id}` | |
| `Template` | `delete(id)` | DELETE | `/templates/{id}` | |
| `Template` | `listTemplateFields(templateId)` | GET | `/templates/{id}/template_fields` | |
| `TemplateField` | `list()` | GET | `/template_fields` | |
| `TemplateField` | `update(id, params)` | PATCH | `/template_fields/{id}` | |
| `TemplateField` | `delete(id)` | DELETE | `/template_fields/{id}` | |
| `Membership` | `list()` | GET | `/memberships` | |
| `Membership` | `filter()` | GET | `/memberships` | Q: `role`, `user.id` |
| `Membership` | `retrieve(id)` | GET | `/memberships/{id}` | |
| `Membership` | `create(params)` | POST | `/memberships` | |
| `Membership` | `update(id, params)` | **PUT** | `/memberships/{id}` | PUT — não PATCH (Ruby/Python devem espelhar) |
| `Membership` | `delete(id)` | DELETE | `/memberships/{id}` | |
| `Group` | `list()` | GET | `/groups` | |
| `Group` | `retrieve(id)` | GET | `/groups/{id}` | |
| `Group` | `create(params)` | POST | `/groups` | |
| `Group` | `update(id, params)` | PATCH | `/groups/{id}` | |
| `Group` | `delete(id)` | DELETE | `/groups/{id}` | |
| `Group` | `addUsers(groupId, ids)` | POST | `/groups/{id}/relationships/users` | |
| `Group` | `removeUsers(groupId, ids)` | DELETE | `/groups/{id}/relationships/users` | body JSON |
| `AccessControlList` | `create(folderId, groupId)` | POST | `/access_control_lists` | só relationships |
| `AccessControlList` | `destroy(folderId, groupId)` | DELETE | `/access_control_lists` | body JSON |

## Extensões API 3.0

| Resource | Método SDK | HTTP | Path | Notas |
|----------|------------|------|------|-------|
| `EnvelopeBulkCreation` | `create(params)` | POST | `/envelope_bulk_creations` | job assíncrono |
| `AcceptanceTermWhatsapp` | `list()` | GET | `/acceptance_term/whatsapps` | Click.Agree |
| `AcceptanceTermWhatsapp` | `filter()` | GET | `/acceptance_term/whatsapps` | Q |
| `AcceptanceTermWhatsapp` | `retrieve(id)` | GET | `/acceptance_term/whatsapps/{id}` | |
| `AcceptanceTermWhatsapp` | `create(params)` | POST | `/acceptance_term/whatsapps` | |
| `AcceptanceTermWhatsapp` | `cancel(id)` | PATCH | `/acceptance_term/whatsapps/{id}` | atalho: `status=canceled` |
| `AcceptanceTermWhatsapp` | `update(id, params)` | PATCH | `/acceptance_term/whatsapps/{id}` | |
| `AutoSignatureTerm` | `retrieve(id)` | GET | `/auto_signature/terms/{id}` | |
| `AutoSignatureTerm` | `create(params)` | POST | `/auto_signature/terms` | |

## Utilitários (fora de `ClicksignClient`)

| Classe | Método | Descrição |
|--------|--------|-----------|
| `WebhookValidator` | `verifySignature` / `isValidSignature` | HMAC-SHA256 do payload |

## Tipagem

Enums e value objects: [TYPING.md](TYPING.md).

Documentação oficial completa: [llms.txt](https://developers.clicksign.com/llms.txt).
