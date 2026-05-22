package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
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
            .environment(Environment.SANDBOX)
            .build();

        List<Envelope> page1 = client.envelopes().filter()
            .status(EnvelopeStatus.RUNNING)
            .name("Contrato Q1")
            .order("-created")
            .page(1)
            .perPage(25)
            .fetch();

        List<Envelope> allRunning = client.envelopes().filter()
            .status(EnvelopeStatus.RUNNING)
            .fetchAll();

        List<Document> docs = client.documents()
            .filter("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .status(DocumentStatus.DRAFT)
            .fetch();

        List<AcceptanceTermWhatsapp> whatsapps = client.acceptanceTermWhatsapps().filter()
            .status(AcceptanceTermStatus.SENT)
            .fetch();

        client.envelopes().filter()
            .status(EnvelopeStatus.RUNNING)
            .include("documents", "signers")
            .fields("envelopes", "name", "status")
            .fetch();

        System.out.printf("página=%d, total running=%d, docs=%d, whatsapps=%d%n",
            page1.size(), allRunning.size(), docs.size(), whatsapps.size());
    }
}
