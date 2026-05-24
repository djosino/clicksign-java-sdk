package com.clicksign.http;

import com.clicksign.ClientConfig;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.RateLimitException;
import com.clicksign.errors.ServiceUnavailableException;
import com.clicksign.errors.TimeoutException;
import com.clicksign.instrumentation.ErrorEvent;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.instrumentation.RequestEvent;
import com.clicksign.instrumentation.RetryEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Low-level HTTP client backed by {@code java.net.http.HttpClient} (Java 11+).
 *
 * <p>Zero runtime dependencies. Handles retry with full-jitter exponential backoff,
 * JSON:API headers, maps HTTP status codes to typed exceptions, and publishes
 * instrumentation events. {@code maxRetries=N} means N retries after the first
 * attempt (N+1 total attempts).
 */
public final class HttpClient {

    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final double BACKOFF_BASE_SECONDS = 0.5;
    private static final double BACKOFF_CAP_SECONDS = 30.0;

    private final ClientConfig config;
    private final Instrumentation instrumentation;
    private final java.net.http.HttpClient delegate;

    /**
     * Constructs the HTTP client.
     *
     * @param config          client configuration
     * @param instrumentation instrumentation registry
     */
    public HttpClient(ClientConfig config, Instrumentation instrumentation) {
        this.config = config;
        this.instrumentation = instrumentation;
        this.delegate = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.connectTimeoutMs()))
            .build();
    }

    /**
     * Performs a GET request.
     *
     * @param path   API path
     * @param params query parameters
     * @return response body
     */
    public String get(String path, Map<String, String> params) {
        String url = buildUrl(path, params);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .GET()
            .build();
        return executeWithRetry(request, path);
    }

    /**
     * Performs a POST request.
     *
     * @param path API path
     * @param body request body
     * @return response body
     */
    public String post(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return executeWithRetry(request, path);
    }

    /**
     * Performs a PATCH request.
     *
     * @param path API path
     * @param body request body
     * @return response body
     */
    public String patch(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();
        return executeWithRetry(request, path);
    }

    /**
     * Performs a PUT request.
     *
     * @param path API path
     * @param body request body
     * @return response body
     */
    public String put(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return executeWithRetry(request, path);
    }

    /**
     * Performs a DELETE request.
     *
     * @param path API path
     * @param body optional request body
     */
    public void delete(String path, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey());

        if (body != null && !body.isEmpty()) {
            builder.method("DELETE", HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        }
        executeWithRetry(builder.build(), path);
    }

    private String executeWithRetry(HttpRequest request, String resourcePath) {
        int attempts = 0;
        while (true) {
            attempts++;
            long start = System.nanoTime();
            try {
                HttpResponse<String> response = delegate.send(
                    request, HttpResponse.BodyHandlers.ofString());
                double durationMs = elapsedMs(start);
                int status = response.statusCode();

                instrumentation.publishRequest(new RequestEvent(
                    method(request), resourcePath, status, attempts, durationMs));

                String result;
                try {
                    result = handleResponse(response);
                } catch (ClicksignException e) {
                    instrumentation.publishError(new ErrorEvent(
                        method(request), resourcePath, status, e, durationMs));
                    throw e;
                }
                return result;

            } catch (IOException e) {
                double durationMs = elapsedMs(start);
                TimeoutException timeout = new TimeoutException(e.getMessage(), e);
                if (attempts > config.maxRetries()) {
                    instrumentation.publishError(new ErrorEvent(
                        method(request), resourcePath, 0, timeout, durationMs));
                    throw timeout;
                }
                long waitMs = sleepBeforeRetry(attempts, null);
                instrumentation.publishRetry(new RetryEvent(
                    method(request), resourcePath, attempts, config.maxRetries(), timeout, waitMs));

            } catch (ClicksignException e) {
                if (!e.isRetryable() || attempts > config.maxRetries()) {
                    throw e;
                }
                long waitMs = sleepBeforeRetry(attempts, retryAfterSeconds(e));
                instrumentation.publishRetry(new RetryEvent(
                    method(request), resourcePath, attempts, config.maxRetries(), e, waitMs));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                TimeoutException timeout = new TimeoutException("Request interrupted", e);
                instrumentation.publishError(new ErrorEvent(
                    method(request), resourcePath, 0, timeout, elapsedMs(start)));
                throw timeout;
            }
        }
    }

    private String handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            String body = response.body();
            return (body == null || body.isBlank()) ? null : body;
        }
        String requestId = response.headers().firstValue("x-request-id").orElse(null);
        String body = response.body();
        String message = ErrorMessageExtractor.extract(body, response);
        Long retryAfter = ErrorMessageExtractor.parseRetryAfter(response);
        throw ErrorMessageExtractor.buildException(status, message, requestId, body, retryAfter);
    }

    private static Long retryAfterSeconds(ClicksignException e) {
        if (e instanceof RateLimitException) {
            return ((RateLimitException) e).retryAfterSeconds();
        }
        if (e instanceof ServiceUnavailableException) {
            return ((ServiceUnavailableException) e).retryAfterSeconds();
        }
        return null;
    }

    /** Uses {@code Retry-After} when provided; otherwise full-jitter exponential backoff. */
    private static long sleepBeforeRetry(int attempt, Long retryAfterSeconds) {
        if (retryAfterSeconds != null && retryAfterSeconds > 0) {
            long millis = retryAfterSeconds * 1000;
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return millis;
        }
        return sleepJitter(attempt);
    }

    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(config.baseUrl()).append(path);
        if (params != null && !params.isEmpty()) {
            url.append('?');
            params.forEach((k, v) -> {
                if (v != null) {
                    url.append(encode(k)).append('=').append(encode(v)).append('&');
                }
            });
            url.setLength(url.length() - 1);
        }
        return url.toString();
    }

    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }

    private static double elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000.0;
    }

    private static String method(HttpRequest request) {
        return request.method().toLowerCase();
    }

    /** Sleeps with full jitter and returns actual wait time in milliseconds. */
    private static long sleepJitter(int attempt) {
        double ceiling = Math.min(
            BACKOFF_BASE_SECONDS * Math.pow(2, attempt - 1), BACKOFF_CAP_SECONDS);
        long millis = (long) (ThreadLocalRandom.current().nextDouble() * ceiling * 1000);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return millis;
    }
}
