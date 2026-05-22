package com.clicksign.examples;

import com.clicksign.ClicksignClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Espelha {@code docs/examples/08-production-limitations.md}. */
public final class ProductionLimitationsExample {

    public static void main(String[] args) throws Exception {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(ExampleSupport.environment())
            .maxRetries(3)
            .readTimeoutMs(30_000)
            .build();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            pool.submit(() -> {
                var envelopes = client.envelopes().filter().page(1).perPage(1).fetch();
                String id = envelopes.isEmpty()
                    ? "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                    : envelopes.get(0).id();
                client.envelopes().retrieve(id);
            });
            pool.shutdown();
        } finally {
            if (!pool.isShutdown()) {
                pool.shutdownNow();
            }
        }
    }
}
