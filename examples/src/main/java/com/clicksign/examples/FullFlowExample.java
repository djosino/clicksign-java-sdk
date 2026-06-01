package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.resources.notarial.BulkRequirement;
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.notarial.NotificationParams;
import com.clicksign.resources.notarial.Signer;
import com.clicksign.resources.types.EnvelopeStatus;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;

/**
 * Fluxo completo: criar envelope → documento → signatário → requisitos → ativar → notificar.
 *
 * <pre>
 *   CLICKSIGN_API_KEY=... ./gradlew examples:runFullFlow
 * </pre>
 */
public final class FullFlowExample {

    public static void main(String[] args) {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(ExampleSupport.environment())
            .onRequest(e -> System.out.printf("[HTTP] %s %s → %d (%.0fms)%n",
                e.method(), e.path(), e.status(), e.durationMs()))
            .build();

        // 1. Envelope
        Envelope envelope = client.envelopes().create(
            Envelope.CreateParams.builder()
                .name("Contrato Full Flow SDK")
                .locale("pt-BR")
                .build());
        System.out.println("Envelope criado: " + envelope.id() + " status=" + envelope.status());

        // 2. Documento
        Document document = client.documents().create(
            Document.CreateParams.builder()
                .envelopeId(envelope.id())
                .filename("contrato.pdf")
                .contentUrl(ExampleSupport.SAMPLE_PDF_URL)
                .build());
        System.out.println("Documento criado: " + document.id());

        // 3. Signatário
        Signer signer = client.signers().create(
            Signer.CreateParams.builder()
                .envelopeId(envelope.id())
                .name("Maria Teste")
                .email("maria.sdk.test+" + System.currentTimeMillis() + "@example.com")
                .build());
        System.out.println("Signatário criado: " + signer.id() + " email=" + signer.email());

        // 4. Requisitos (agree + provide_evidence obrigatórios para ativar)
        BulkRequirement.Response req = client.bulkRequirements().create(
            envelope.id(),
            ops -> ops
                .addAgree(signer.id(), document.id(), RequirementRole.SIGN)
                .addProvideEvidence(signer.id(), document.id(), RequirementAuth.EMAIL));

        if (!req.isSuccess()) {
            req.failures().forEach(f -> System.err.println("Falha req idx=" + f.index() + " " + f.errors()));
            System.exit(1);
        }
        req.requirements().forEach(r ->
            System.out.println("Requisito criado: " + r.id() + " action=" + r.action()));

        // 5. Ativar
        client.envelopes().update(envelope.id(),
            Envelope.UpdateParams.builder().status(EnvelopeStatus.RUNNING).build());
        System.out.println("Envelope ativado (running).");

        // 6. Notificar todos os signatários
        client.envelopes().notifyAll(envelope.id(),
            NotificationParams.builder()
                .message("Por favor assine o contrato.")
                .build());
        System.out.println("Notificação enviada para todos os signatários.");

        System.out.println("\nFluxo completo! Envelope: " + envelope.id());
    }
}
