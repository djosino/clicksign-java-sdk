package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.resources.notarial.BulkRequirement;
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.notarial.Signer;
import com.clicksign.resources.types.EnvelopeStatus;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RubricateKind;

/** Espelha {@code docs/examples/02-bulk-requirements.md} — cria recursos reais no sandbox. */
public final class BulkRequirementsExample {

    public static void main(String[] args) {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(ExampleSupport.environment())
            .build();

        Envelope envelope = client.envelopes().create(
            Envelope.CreateParams.builder()
                .name("Exemplo Bulk SDK")
                .locale("pt-BR")
                .build());
        String envelopeId = envelope.id();

        Document document = client.documents().create(
            Document.CreateParams.builder()
                .envelopeId(envelopeId)
                .filename("exemplo-bulk.pdf")
                .contentUrl(ExampleSupport.SAMPLE_PDF_URL)
                .build());

        Signer signer = client.signers().create(
            Signer.CreateParams.builder()
                .envelopeId(envelopeId)
                .name("João Silva")
                .email("joao.bulk.example+" + System.currentTimeMillis() + "@example.com")
                .build());

        String signerId = signer.id();
        String documentId = document.id();

        // agree + provide_evidence são obrigatórios antes de ativar o envelope
        BulkRequirement.Response response = client.bulkRequirements().create(
            envelopeId,
            ops -> ops
                .addAgree(signerId, documentId, RequirementRole.SIGN)
                .addProvideEvidence(signerId, documentId, RequirementAuth.EMAIL)
                // pages: use "all" ou lista aceita pela API — "1-3" costuma falhar em 422
                .addRubricate(signerId, documentId, "all", null, RubricateKind.INITIALS.apiValue()));

        if (response.isSuccess()) {
            response.requirements().forEach(r ->
                System.out.println("requirement " + r.id() + " action=" + r.action()));
        } else {
            for (BulkRequirement.OperationResult slot : response.failures()) {
                System.err.println("falha no índice " + slot.index() + " op=" + slot.op());
                slot.errors().forEach(System.err::println);
            }
            System.exit(1);
        }

        client.envelopes().update(envelopeId,
            Envelope.UpdateParams.builder().status(EnvelopeStatus.RUNNING).build());

        System.out.println("Envelope " + envelopeId + " ativado (running).");
    }
}
