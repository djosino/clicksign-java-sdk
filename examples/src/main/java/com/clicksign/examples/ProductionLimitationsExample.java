package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Espelha {@code docs/examples/08-production-limitations.md}. */
public final class ProductionLimitationsExample {

    public static void main(String[] args) throws Exception {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(Environment.PRODUCTION)
            .maxRetries(3)
            .readTimeoutMs(30_000)
            .build();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            pool.submit(() -> client.envelopes()
                .retrieve("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            pool.shutdown();
        } finally {
            if (!pool.isShutdown()) {
                pool.shutdownNow();
            }
        }
    }
}
