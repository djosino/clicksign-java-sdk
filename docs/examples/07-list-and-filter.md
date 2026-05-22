# Listagem, filtros e paginação

**Quando usar:** listar envelopes/documentos/signatários com filtros, ordenação, páginas manuais ou auto-paginação.

## Código

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.AcceptanceTermStatus;
import com.clicksign.resources.types.DocumentStatus;
import com.clicksign.resources.types.EnvelopeStatus;
import com.clicksign.resources.types.MembershipRole;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .build();

// Uma página — status + ordenação
java.util.List<Envelope> page1 = client.envelopes().filter()
    .status(EnvelopeStatus.RUNNING)
    .order("-created")
    .page(1)
    .perPage(25)
    .fetch();

// Todas as páginas (segue links.next)
java.util.List<Envelope> allRunning = client.envelopes().filter()
    .status(EnvelopeStatus.RUNNING)
    .fetchAll();

// Documentos — use um envelopeId real da sua conta (listar antes evita 404)
java.util.List<Envelope> sample = client.envelopes().filter().page(1).perPage(1).fetch();
if (!sample.isEmpty()) {
    String envelopeId = sample.get(0).id();
    java.util.List<com.clicksign.resources.notarial.Document> docs =
        client.documents().filter(envelopeId)
            .status(DocumentStatus.DRAFT)
            .fetch();
}

// Aceites WhatsApp
java.util.List<com.clicksign.resources.AcceptanceTermWhatsapp> whatsapps =
    client.acceptanceTermWhatsapps().filter()
        .status(AcceptanceTermStatus.SENT)
        .fetch();

// Membros — role + usuário + sort
java.util.List<com.clicksign.resources.Membership> admins = client.memberships().filter()
    .role(MembershipRole.ADMIN)
    .userId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
    .order("-created")
    .fetch();

// Incluir relacionamentos (quando a API suportar)
client.envelopes().filter()
    .status(EnvelopeStatus.RUNNING)
    .include("documents", "signers")
    .fields("envelopes", "name", "status")
    .fetch();

// Legado: filter(String, String) ainda funciona
client.envelopes().filter().filter("status", "running").fetch();
```

## O que está acontecendo

- `filter()` retorna `ResourceQuery<T>` — métodos encadeados montam query string
- `filter("chave", valor)` → `filter[chave]=valor`
- `order("-created")` envia `sort=-created` na query
- `page(n)` / `perPage(n)` → `page[number]` e `page[size]`
- `include(...)` e `fields(type, ...)` mapeiam parâmetros JSON:API opcionais
- `fetchAll()` percorre `links.next`; se ausente, usa heurística (página cheia → tenta próxima)

## Erros comuns

- UUID placeholder (`aaaaaaaa-...`) em `documents().filter(envelopeId)` — **404** se o envelope não existir; liste envelopes e use `sample.get(0).id()`
- `fetchAll()` em contas com milhões de registros sem filtro — pode ser lento; prefira filtros estreitos
- Assumir ordem estável entre páginas sem `order()` explícito
- Confundir `list()` (primeira página padrão da API) com `filter().fetchAll()` (todas as páginas)

Mapa de endpoints: [SPEC.md](../SPEC.md).
