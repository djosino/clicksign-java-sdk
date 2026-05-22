package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.resources.AcceptanceTermWhatsapp;
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.AcceptanceTermStatus;
import com.clicksign.resources.types.DocumentStatus;
import com.clicksign.resources.types.EnvelopeStatus;

import java.util.List;

/** Espelha {@code docs/examples/07-list-and-filter.md}. */
public final class ListAndFilterExample {

    public static void main(String[] args) {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(ExampleSupport.environment())
            .build();

        // Envelopes — não usa UUID fixo; funciona em qualquer conta sandbox
        List<Envelope> page1 = client.envelopes().filter()
            .status(EnvelopeStatus.RUNNING)
            .order("-created")
            .page(1)
            .perPage(25)
            .fetch();

        List<Envelope> allRunning = client.envelopes().filter()
            .status(EnvelopeStatus.RUNNING)
            .fetchAll();

        // Documentos — só se existir envelope na conta (evita 404 de UUID placeholder)
        int docsCount = 0;
        List<Envelope> sample = client.envelopes().filter().page(1).perPage(1).fetch();
        if (!sample.isEmpty()) {
            String envelopeId = sample.get(0).id();
            docsCount = client.documents().filter(envelopeId)
                .status(DocumentStatus.DRAFT)
                .fetch()
                .size();
            System.out.println("Documentos (draft) do envelope " + envelopeId + ": " + docsCount);
        } else {
            System.out.println("Nenhum envelope na conta — exemplo de documentos omitido.");
        }

        List<AcceptanceTermWhatsapp> whatsapps = client.acceptanceTermWhatsapps().filter()
            .status(AcceptanceTermStatus.SENT)
            .fetch();

        List<Envelope> withIncludes = client.envelopes().filter()
            .status(EnvelopeStatus.RUNNING)
            .include("documents", "signers")
            .fields("envelopes", "name", "status")
            .fetch();

        System.out.printf(
            "envelopes página=%d, running total=%d, whatsapps sent=%d, running com include=%d%n",
            page1.size(), allRunning.size(), whatsapps.size(), withIncludes.size());
    }
}
