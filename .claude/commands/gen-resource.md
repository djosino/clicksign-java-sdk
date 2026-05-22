Gere um novo resource para o SDK em src/main/java/com/clicksign/resources/$ARGUMENTS.java.

## Passo 1 — Fontes da verdade (ler nesta ordem)

1. **API docs**: `https://developers.clicksign.com/llms.txt` — lista todos os endpoints disponíveis
2. **Campos**: `https://developers.clicksign.com/reference/{name}-campos-e-regras-de-negocio.md`
3. **Operações**: páginas de referência de cada endpoint do resource
4. **TODO**: `docs/TODO.md` — itens pendentes ou restrições conhecidas

## Passo 2 — Namespace e caminho

| Resources | Pacote | Caminho |
|-----------|--------|---------|
| Envelope, Document, Signer, Requirement, BulkRequirement, SignatureWatcher, Event | `com.clicksign.resources.notarial` | `src/main/java/com/clicksign/resources/notarial/{Name}.java` |
| Demais | `com.clicksign.resources` | `src/main/java/com/clicksign/resources/{Name}.java` |

## Passo 3 — Estrutura obrigatória

```java
public final class {Name} {

    // campos private final
    private final String id;
    // ...

    private {Name}(JsonApiParser.ResourceObject obj) { ... }

    // getters sem prefixo get
    public String id() { return id; }

    private static String str(Object o)   { return o != null ? o.toString() : null; }
    private static boolean bool(Object o) { return Boolean.TRUE.equals(o) || "true".equals(str(o)); }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {
        private final HttpClient http;
        public Service(HttpClient http) { this.http = http; }

        // apenas métodos que a API documenta
        public List<{Name}> list() { ... }
        public {Name} retrieve(String id) { ... }
        public {Name} create(CreateParams params) { ... }
        public {Name} update(String id, UpdateParams params) { ... }
        public void delete(String id) { ... }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {
        // campos
        private CreateParams(Builder b) { ... }
        Map<String, Object> toAttributes() { ... }
        Map<String, Object> toRelationships() { ... } // se tiver relacionamentos

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            // build() lança IllegalArgumentException para campos obrigatórios ausentes
        }
    }

    // ── UpdateParams (se API suporta update) ─────────────────────────────────

    public static final class UpdateParams { ... }
}
```

## Regras

- Implementar **apenas** métodos que a API documenta
- `Membership.update` → `http.put()`, não `http.patch()`
- Resources nested em envelope: path `"/envelopes/" + envelopeId + "/{resources}"` com `parentEnvelopeId` no construtor
- `communicate_events` → `Map<String, Object>`, nunca `String`
- Objetos compostos (ex: `signature_host`) → inner class ou `Map<String, Object>`
- `metadata` → `Map<String, Object>`, nunca `String`
- Getters sem prefixo `get` — `name()`, não `getName()`

## Passo 4 — Expor no ClicksignClient

Adicionar `private final {Name}.Service {names};` e getter correspondente em `ClicksignClient.java`.

## Passo 5 — Testes

Criar `src/test/java/com/clicksign/resources/{module}/{Name}Test.java`.

### Estrutura mínima

```java
@ExtendWith(WireMockExtension.class)
class {Name}Test {

    // UUIDs: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" — nunca UUIDs reais

    @Test
    void list_returnsCollection() { ... }

    @Test
    void retrieve_returnsSingle() { ... }

    @Test
    void create_returnsCreated() { ... }

    @Test
    void create_missingRequired_throwsIllegalArgument() { ... }

    @Test
    void update_returnsUpdated() { ... }  // se suportado

    @Test
    void delete_sendsDelete() { ... }  // se suportado
}
```

### Regras de teste
- Stubs HTTP via WireMock
- Fixtures via `JsonApiFixtures` — adicionar método se necessário
- Verificar método HTTP, path e body enviado
- Testar erro 422: `JsonApiFixtures.errorBody("msg")` → `ValidationException`
- Testar campos obrigatórios ausentes → `IllegalArgumentException`

## Passo 6 — Atualizar docs/TODO.md

Marcar item como concluído ou adicionar novos gaps encontrados.
