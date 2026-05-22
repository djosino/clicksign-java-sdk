package com.clicksign.jsonapi;

import com.clicksign.ClientConfig;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.TimeoutException;
import com.clicksign.http.ErrorMessageExtractor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * HTTP client for JSON:API Atomic Operations (bulk endpoints).
 *
 * <p>Unlike the main {@code HttpClient}, this client:
 * <ul>
 *   <li>Only retries on {@link TimeoutException}, not on {@link com.clicksign.errors.ServerException}.</li>
 *   <li>Returns the raw response body even on 4xx/5xx when {@code atomic:results} is present.</li>
 * </ul>
 */
public final class BulkOperationsClient {

    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final double BACKOFF_BASE_SECONDS = 0.5;
    private static final double BACKOFF_CAP_SECONDS  = 30.0;

    private final ClientConfig config;
    private final java.net.http.HttpClient delegate;

    /**
     * Constructs a BulkOperationsClient using the provided configuration.
     *
     * @param config client configuration (base URL, API key, timeouts, retries)
     */
    public BulkOperationsClient(ClientConfig config) {
        this.config   = config;
        this.delegate = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.connectTimeoutMs()))
            .build();
    }

    /**
     * Sends a POST request to the given path with the specified JSON body.
     *
     * @param path relative API path
     * @param body JSON request body
     * @return raw response body
     */
    public String post(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept",       CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return executeWithRetry(request);
    }

    private String executeWithRetry(HttpRequest request) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                HttpResponse<String> response = delegate.send(request, HttpResponse.BodyHandlers.ofString());
                return handleResponse(response);
            } catch (IOException e) {
                TimeoutException timeout = new TimeoutException(e.getMessage(), e);
                if (attempts > config.maxRetries()) {
                    throw timeout;
                }
                sleepJitter(attempts);
            } catch (TimeoutException e) {
                if (attempts > config.maxRetries()) {
                    throw e;
                }
                sleepJitter(attempts);
            } catch (ClicksignException e) {
                // BulkOperationsClient does NOT retry ServerException — only TimeoutException.
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeoutException("Request interrupted", e);
            }
        }
    }

    private String handleResponse(HttpResponse<String> response) {
        String body = response.body();
        // If atomic:results is present, return body regardless of HTTP status.
        if (body != null && body.contains("\"atomic:results\"")) {
            return body;
        }
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return body == null || body.isBlank() ? null : body;
        }
        String requestId = response.headers().firstValue("x-request-id").orElse(null);
        String message   = ErrorMessageExtractor.extract(body, response);
        throw ErrorMessageExtractor.buildException(status, message, requestId, body, null);
    }

    private static void sleepJitter(int attempt) {
        double ceiling = Math.min(BACKOFF_BASE_SECONDS * Math.pow(2, attempt - 1), BACKOFF_CAP_SECONDS);
        long millis = (long) (ThreadLocalRandom.current().nextDouble() * ceiling * 1000);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
