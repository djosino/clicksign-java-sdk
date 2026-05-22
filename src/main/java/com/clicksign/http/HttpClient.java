package com.clicksign.http;

import com.clicksign.ClientConfig;
import com.clicksign.errors.*;

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
 * JSON:API headers, and maps HTTP status codes to typed exceptions.
 */
public final class HttpClient {

    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final double BACKOFF_BASE_SECONDS = 0.5;
    private static final double BACKOFF_CAP_SECONDS  = 30.0;

    private final ClientConfig config;
    private final java.net.http.HttpClient delegate;

    public HttpClient(ClientConfig config) {
        this.config = config;
        this.delegate = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.connectTimeoutMs()))
            .build();
    }

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
        return executeWithRetry(request);
    }

    public String post(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return executeWithRetry(request);
    }

    public String patch(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(config.baseUrl() + path))
            .timeout(Duration.ofMillis(config.readTimeoutMs()))
            .header("Content-Type", CONTENT_TYPE)
            .header("Accept", CONTENT_TYPE)
            .header("Authorization", config.apiKey())
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();
        return executeWithRetry(request);
    }

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
        executeWithRetry(builder.build());
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
                if (attempts > config.maxRetries()) throw timeout;
                sleepJitter(attempts);
            } catch (ClicksignException e) {
                if (!e.isRetryable() || attempts > config.maxRetries()) throw e;
                sleepJitter(attempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeoutException("Request interrupted", e);
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
        String body      = response.body();
        String message   = ErrorMessageExtractor.extract(body, response);
        throw buildException(status, message, requestId, body);
    }

    private ClicksignException buildException(int status, String message, String requestId, String body) {
        if (status == 401 || status == 403) return new AuthenticationException(message, status, requestId, body);
        if (status == 404)                  return new NotFoundException(message, status, requestId, body);
        if (status == 400 || status == 422) return new ValidationException(message, status, requestId, body);
        if (status == 409)                  return new ConflictException(message, status, requestId, body);
        if (status == 429)                  return new RateLimitException(message, status, requestId, body);
        if (status >= 500)                  return new ServerException(message, status, requestId, body);
        return new ClicksignException(message, status, requestId, body);
    }

    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(config.baseUrl()).append(path);
        if (params != null && !params.isEmpty()) {
            url.append('?');
            params.forEach((k, v) -> url.append(encode(k)).append('=').append(encode(v)).append('&'));
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
