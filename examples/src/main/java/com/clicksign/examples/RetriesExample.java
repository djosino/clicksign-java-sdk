package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.RateLimitException;
import com.clicksign.errors.ServerException;

/** Espelha {@code docs/examples/01-retries.md}. */
public final class RetriesExample {

    public static void main(String[] args) {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(Environment.PRODUCTION)
            .maxRetries(3)
            .connectTimeoutMs(3_000)
            .readTimeoutMs(15_000)
            .onRetry(event -> System.out.printf("retry %d, aguardando %dms%n",
                event.attempt(), event.waitMs()))
            .build();

        try {
            client.envelopes().retrieve("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        } catch (RateLimitException e) {
            Long retryAfter = e.retryAfterSeconds();
            System.out.println("rate limit após retries; retryAfter=" + retryAfter);
        } catch (ServerException e) {
            if (e.isRetryable()) {
                System.out.println("server error retryable: " + e.getMessage());
            }
        } catch (ClicksignException e) {
            System.out.println("erro não retryable: " + e.getMessage());
        }
    }
}
