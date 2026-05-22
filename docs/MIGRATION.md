# Notas de migração (SDK)

Este SDK cobre apenas a **Clicksign API 3.0 (Envelope)**.

## Guia oficial de migração

Siga a documentação do produto para mudanças conceituais e de endpoints:

- [Guia de migração](https://developers.clicksign.com/docs/guia-de-migracao.md)
- [Comparativo técnico 1.9 vs 3.0](https://developers.clicksign.com/docs/comparativo-tecnico.md)
- [FAQ migração](https://developers.clicksign.com/docs/faq-migracao.md)

## Breaking changes específicos do SDK

| Tópico | API 1.9 / SDK antigo | SDK API 3.0 |
|--------|----------------------|-------------|
| Modelo envelope | Documentos/signatários “flat” | Aninhados em `/envelopes/{id}/...` |
| Metadata | JSON em string | `Map<String, Object>` |
| Folder | update/delete | apenas create, list, retrieve |
| Notificação signatário | `Signer.NotifyParams` | `NotificationParams` |
| Listagem de requisitos | helpers por recurso | `requirements().list(envelopeId)` |
| Assinatura presencial | maps ad hoc | `Signer.SignatureHost` + `CommunicateEvents` opcional |

## Tipagem de `communicate_events`

Código legado com `Map<String, Object>` continua funcionando. Código novo pode usar:

```java
CommunicateEvents.builder()
    .signatureRequest(NotificationChannel.EMAIL)
    .build()
```

Em respostas, use `signer.communicateEventsConfig()` quando precisar de acesso tipado.

Mais enums e value objects: [TYPING.md](TYPING.md).
