# Bulk requirements (operações atômicas)

**Quando usar:** criar ou remover vários requisitos de assinatura em uma única requisição, com resultado por operação.

## Código

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.resources.notarial.BulkRequirement;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RubricateKind;

ClicksignClient client = ClicksignClient.builder()
    .apiKey(System.getenv("CLICKSIGN_API_KEY"))
    .environment(Environment.SANDBOX)
    .build();

String envelopeId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
String signerId   = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
String documentId = "cccccccc-cccc-cccc-cccc-cccccccccccc";

BulkRequirement.Response response = client.bulkRequirements().create(
    envelopeId,
    ops -> ops
        .addAgree(signerId, documentId, RequirementRole.SIGN)
        .addProvideEvidence(signerId, documentId, RequirementAuth.EMAIL)
        .addRubricate(signerId, documentId, "1-3", null, RubricateKind.INITIALS.apiValue())
);

if (response.isSuccess()) {
    response.requirements().forEach(r ->
        System.out.println("requirement " + r.id() + " action=" + r.action()));
} else {
    for (BulkRequirement.OperationResult slot : response.failures()) {
        System.err.println("falha no índice " + slot.index() + " op=" + slot.op());
        slot.errors().forEach(err -> System.err.println(err));
    }
}

// Remover requisito existente
client.bulkRequirements().create(envelopeId,
    ops -> ops.remove("dddddddd-dddd-dddd-dddd-dddddddddddd"));
```

## O que está acontecendo

- O builder `Operations` monta o array `atomic:operations` (`add` / `remove`)
- `BulkOperationsClient` envia POST para `/envelopes/{id}/bulk_requirements`
- Se o corpo contém `atomic:results`, o SDK parseia cada slot — sucesso ou lista de `errors` por índice
- Falha parcial **não** lança exceção automaticamente — inspecione `response.isSuccess()`

## Erros comuns

- Assumir exceção quando um slot falha — verifique `failures()`
- Reenviar o lote inteiro após timeout sem checar o que já foi criado — bulk só retenta timeout, não 5xx
- Omitir `documentId`/`signerId` nas operações `add` — relationships são obrigatórias
- Ativar o envelope só com `addAgree` — antes de `update` com `status` `running` (ou `activate`), inclua também `addProvideEvidence` para o mesmo par signatário/documento

Arquitetura: [ARCHITECTURE.md](../ARCHITECTURE.md#operações-em-massa-bulk).
