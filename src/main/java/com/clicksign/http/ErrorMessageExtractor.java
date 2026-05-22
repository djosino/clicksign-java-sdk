package com.clicksign.http;

import com.clicksign.errors.AuthenticationException;
import com.clicksign.errors.ClicksignException;
import com.clicksign.errors.ConflictException;
import com.clicksign.errors.NotFoundException;
import com.clicksign.errors.RateLimitException;
import com.clicksign.errors.ServerException;
import com.clicksign.errors.ServiceUnavailableException;
import com.clicksign.errors.ValidationException;
import com.clicksign.jsonapi.MinimalJsonParser;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/** Utility for extracting error messages from JSON:API error responses. */
public final class ErrorMessageExtractor {

    private ErrorMessageExtractor() {}

    /**
     * Extracts a human-readable error message from the response body.
     *
     * @param body     raw response body
     * @param response HTTP response
     * @return extracted error message
     */
    public static String extract(String body, HttpResponse<String> response) {
        if (body == null || body.isBlank()) {
            return response.toString();
        }
        try {
            if (!body.contains("\"errors\"")) {
                return response.toString();
            }
            Map<String, Object> root = MinimalJsonParser.parseObject(body);
            Object errors = root.get("errors");
            if (!(errors instanceof List)) {
                return response.toString();
            }

            StringBuilder messages = new StringBuilder();
            for (Object item : (List<?>) errors) {
                if (!(item instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> err = (Map<String, Object>) item;
                String part = firstNonBlank(str(err.get("detail")), str(err.get("title")));
                if (part != null) {
                    if (messages.length() > 0) {
                        messages.append(", ");
                    }
                    messages.append(part);
                }
            }
            return messages.length() > 0 ? messages.toString() : response.toString();
        } catch (Exception e) {
            return response.toString();
        }
    }

    /**
     * Builds the appropriate exception subclass for the given HTTP status.
     *
     * @param status            HTTP status code
     * @param message           error message
     * @param requestId         API request id
     * @param body              raw response body
     * @param retryAfterSeconds retry-after header value in seconds
     * @return exception instance
     */
    public static ClicksignException buildException(int status, String message, String requestId,
                                                    String body, Long retryAfterSeconds) {
        if (status == 401 || status == 403) {
            return new AuthenticationException(message, status, requestId, body);
        }
        if (status == 404) {
            return new NotFoundException(message, status, requestId, body);
        }
        if (status == 400 || status == 422) {
            return new ValidationException(message, status, requestId, body);
        }
        if (status == 409) {
            return new ConflictException(message, status, requestId, body);
        }
        if (status == 429) {
            return new RateLimitException(message, status, requestId, body, retryAfterSeconds);
        }
        if (status == 503) {
            return new ServiceUnavailableException(message, status, requestId, body, retryAfterSeconds);
        }
        if (status >= 500) {
            return new ServerException(message, status, requestId, body);
        }
        return new ClicksignException(message, status, requestId, body);
    }

    static Long parseRetryAfter(HttpResponse<String> response) {
        return response.headers().firstValue("Retry-After")
            .flatMap(ErrorMessageExtractor::parseRetryAfterValue)
            .orElse(null);
    }

    private static java.util.Optional<Long> parseRetryAfterValue(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return java.util.Optional.empty();
        }
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
