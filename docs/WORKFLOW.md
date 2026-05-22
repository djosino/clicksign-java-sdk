# Fluxo completo de assinatura

Guia ponta a ponta para criar um envelope, anexar documento, configurar signatários e requisitos, ativar e notificar — usando o SDK Java da API 3.0.

> **Convenção Java deste SDK:** use `envelope.name()`, não `getName()`. Todos os recursos seguem esse padrão.

## Configuração inicial

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .maxRetries(2)
    .build();
```

## 1. Criar o envelope

```java
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.EnvelopeLocale;

Envelope envelope = client.envelopes().create(
    Envelope.CreateParams.builder()
        .name("Contrato de prestação de serviços")
        .locale(EnvelopeLocale.PT_BR)
        .autoClose(true)
        .build()
);

String envelopeId = envelope.id();
```

## 2. Adicionar o documento

Upload em base64, por modelo ou por duplicata de documento existente:

```java
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.types.DocumentTemplate;

Document document = client.documents().create(
    Document.CreateParams.builder()
        .envelopeId(envelopeId)
        .filename("contrato.pdf")
        .contentBase64(base64Pdf)
        .build()
);

// Alternativa: documento a partir de modelo
Document fromTemplate = client.documents().create(
    Document.CreateParams.builder()
        .envelopeId(envelopeId)
        .filename("contrato.docx")
        .template(DocumentTemplate.withFields("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            java.util.Map.of("nome_cliente", "Empresa XYZ")))
        .build()
);
```

## 3. Adicionar o signatário

```java
import com.clicksign.resources.notarial.Signer;
import com.clicksign.resources.notarial.CommunicateEvents;
import com.clicksign.resources.notarial.NotificationChannel;

Signer signer = client.signers().create(
    Signer.CreateParams.builder()
        .envelopeId(envelopeId)
        .name("João Silva")
        .email("joao@example.com")
        .phoneNumber("11987654321")
        .communicateEvents(CommunicateEvents.builder()
            .signatureRequest(NotificationChannel.EMAIL)
            .build())
        .build()
);
```

Assinatura presencial: configure `signatureHost` no `CreateParams`:

```java
import com.clicksign.resources.notarial.CommunicateEvents;
import com.clicksign.resources.notarial.NotificationChannel;

Signer.SignatureHost host = new Signer.SignatureHost(
    "Maria Recepcionista", "maria@empresa.com",
    CommunicateEvents.builder().signatureRequest(NotificationChannel.EMAIL).build());

Signer.CreateParams.builder()
    .envelopeId(envelopeId)
    .name("João Silva")
    .email("joao@example.com")
    .signatureHost(host)
    .build();
```

## 4. Requisitos de assinatura

Relacione signatário e documento com `documentId` e `signerId` nos builders — o SDK monta o JSON:API `relationships` internamente.

> **Pré-requisito para ativar:** antes de colocar o envelope em `running`, a API exige **pelo menos um requisito `agree`** (qualificação, com `role`) **e um `provide_evidence`** (autenticação, com `auth`) para o mesmo par signatário/documento. Sem os dois, `update` com `status` ou `activate` retorna erro de validação.

### 4.1 Endpoint padrão (`Requirement`)

```java
import com.clicksign.resources.notarial.Requirement;
import com.clicksign.resources.types.RequirementAction;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RequirementAuth;

// Qualificação (concordar + papel)
client.requirements().create(
    Requirement.CreateParams.builder()
        .envelopeId(envelopeId)
        .action(RequirementAction.AGREE)
        .role(RequirementRole.SIGN)
        .documentId(document.id())
        .signerId(signer.id())
        .build()
);

// Autenticação (evidência)
client.requirements().create(
    Requirement.CreateParams.builder()
        .envelopeId(envelopeId)
        .action(RequirementAction.PROVIDE_EVIDENCE)
        .auth(RequirementAuth.EMAIL)
        .documentId(document.id())
        .signerId(signer.id())
        .build()
);
```

### 4.2 Operações em lote (`BulkRequirement`)

Para vários requisitos em uma única chamada atômica:

```java
import com.clicksign.resources.notarial.BulkRequirement;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RequirementAuth;

BulkRequirement.Response response = client.bulkRequirements().create(
    envelopeId,
    ops -> ops
        .addAgree(signer.id(), document.id(), RequirementRole.SIGN)
        .addProvideEvidence(signer.id(), document.id(), RequirementAuth.EMAIL)
);

if (!response.isSuccess()) {
    response.failures().forEach(f ->
        System.err.println("slot " + f.index() + ": " + f.errors()));
}
```

Detalhes: [examples/02-bulk-requirements.md](examples/02-bulk-requirements.md).

## 5. Ativar o envelope

Com os requisitos da seção 4 criados, altere o status para `running` via `update` (recomendado):

```java
import com.clicksign.resources.types.EnvelopeStatus;

Envelope activated = client.envelopes().update(envelopeId,
    Envelope.UpdateParams.builder()
        .status(EnvelopeStatus.RUNNING)
        .build());
// status esperado: "running"
```

Alternativa equivalente: `client.envelopes().activate(envelopeId)` (`POST /envelopes/{id}/activate`). Prefira `update` com `EnvelopeStatus.RUNNING` — o PATCH em `/envelopes/{id}` é o fluxo usual na API 3.0 e permite ajustar outros atributos na mesma chamada.

## 6. Notificar signatários

Todos os signatários do envelope:

```java
import com.clicksign.resources.notarial.NotificationParams;
import com.clicksign.resources.types.EmailCustomization;

client.envelopes().notifyAll(envelopeId,
    NotificationParams.builder()
        .message("Por favor, assine o documento pendente.")
        .emailCustomization(EmailCustomization.builder()
            .subject("Assinatura pendente — Contrato")
            .build())
        .build());
```

Um signatário específico:

```java
client.signers().notify(signer.id(), envelopeId,
    NotificationParams.builder().message("Sua vez de assinar.").build());
```

## 7. Monitorar eventos

```java
import com.clicksign.resources.notarial.Event;

java.util.List<Event> events = client.events().listForEnvelope(envelopeId);
events.forEach(e -> System.out.println(e.name() + " @ " + e.createdAt()));

// Eventos de um documento específico
java.util.List<Event> docEvents = client.documents().listEvents(document.id(), envelopeId);
```

## Consultas com filtro e paginação

```java
java.util.List<Envelope> drafts = client.envelopes().filter()
    .filter("status", "draft")
    .order("-created")
    .page(1)
    .perPage(20)
    .fetch();

java.util.List<Envelope> allDrafts = client.envelopes().filter()
    .filter("status", "draft")
    .fetchAll();
```

Veja [examples/07-list-and-filter.md](examples/07-list-and-filter.md).

## Próximos passos

- [SPEC.md](SPEC.md) — mapa completo de endpoints  
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) — erros comuns  
- [SDK_CONTRACT.md](SDK_CONTRACT.md) — retry, JSON:API, webhooks  
