# Listagem, filtros e paginação

**Quando usar:** listar envelopes/documentos/signatários com filtros, ordenação, páginas manuais ou auto-paginação.

## Código

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.resources.notarial.Envelope;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .build();

// Uma página
java.util.List<Envelope> page1 = client.envelopes().filter()
    .filter("status", "running")
    .order("-created")
    .page(1)
    .perPage(25)
    .fetch();

// Todas as páginas (segue links.next)
java.util.List<Envelope> allRunning = client.envelopes().filter()
    .filter("status", "running")
    .fetchAll();

// Documentos de um envelope
java.util.List<com.clicksign.resources.notarial.Document> docs =
    client.documents().filter("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        .filter("status", "draft")
        .fetch();

// Aceites WhatsApp
java.util.List<com.clicksign.resources.AcceptanceTermWhatsapp> whatsapps =
    client.acceptanceTermWhatsapps().filter()
        .filter("status", "sent")
        .fetch();

// Incluir relacionamentos (quando a API suportar)
client.envelopes().filter()
    .filter("status", "running")
    .include("documents", "signers")
    .fields("envelopes", "name", "status")
    .fetch();
```

## O que está acontecendo

- `filter()` retorna `ResourceQuery<T>` — métodos encadeados montam query string
- `filter("chave", valor)` → `filter[chave]=valor`
- `order("-created")` envia `sort=-created` na query
- `page(n)` / `perPage(n)` → `page[number]` e `page[size]`
- `include(...)` e `fields(type, ...)` mapeiam parâmetros JSON:API opcionais
- `fetchAll()` percorre `links.next`; se ausente, usa heurística (página cheia → tenta próxima)

## Erros comuns

- `fetchAll()` em contas com milhões de registros sem filtro — pode ser lento; prefira filtros estreitos
- Assumir ordem estável entre páginas sem `order()` explícito
- Confundir `list()` (primeira página padrão da API) com `filter().fetchAll()` (todas as páginas)

Mapa de endpoints: [SPEC.md](../SPEC.md).
