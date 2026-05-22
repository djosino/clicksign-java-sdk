package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.errors.AuthenticationException;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.RateLimitException;
import com.clicksign.errors.ServerException;
import com.clicksign.resources.notarial.Envelope;

import java.util.List;

/** Espelha {@code docs/examples/01-retries.md}. */
public final class RetriesExample {

    public static void main(String[] args) {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(ExampleSupport.environment())
            .maxRetries(3)
            .connectTimeoutMs(3_000)
            .readTimeoutMs(15_000)
            .onRetry(event -> System.out.printf("retry %d, aguardando %dms%n",
                event.attempt(), event.waitMs()))
            .build();

        String envelopeId = resolveEnvelopeId(client);
        System.out.println("Ambiente: " + ExampleSupport.environment()
            + " — retrieve envelope " + envelopeId);

        try {
            Envelope envelope = client.envelopes().retrieve(envelopeId);
            System.out.println("OK: " + envelope.id() + " status=" + envelope.status());
        } catch (RateLimitException e) {
            System.out.println("rate limit após retries; retryAfter=" + e.retryAfterSeconds());
        } catch (ServerException e) {
            System.out.println("server error (retryable=" + e.isRetryable() + "): " + e.getMessage());
        } catch (AuthenticationException e) {
            System.err.println("401/403 — verifique CLICKSIGN_API_KEY e CLICKSIGN_ENVIRONMENT (sandbox vs production)");
            System.exit(1);
        } catch (NotFoundException e) {
            System.out.println("404 esperado se o id não existir: " + e.getMessage());
        } catch (ClicksignException e) {
            System.out.println("erro não retryable: " + e.getMessage());
        }
    }

    private static String resolveEnvelopeId(ClicksignClient client) {
        List<Envelope> envelopes = client.envelopes().filter().page(1).perPage(1).fetch();
        if (!envelopes.isEmpty()) {
            return envelopes.get(0).id();
        }
        return "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    }
}
