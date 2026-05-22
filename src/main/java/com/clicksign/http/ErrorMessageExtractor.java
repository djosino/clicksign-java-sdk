package com.clicksign.http;

import java.net.http.HttpResponse;

final class ErrorMessageExtractor {

    private ErrorMessageExtractor() {}

    static String extract(String body, HttpResponse<String> response) {
        if (body == null || body.isBlank()) return response.toString();
        try {
            // Minimal JSON parsing without external dependencies.
            // Extracts errors[*].detail or errors[*].title from JSON:API error body.
            if (!body.contains("\"errors\"")) return response.toString();

            StringBuilder messages = new StringBuilder();
            int idx = 0;
            while ((idx = body.indexOf("\"detail\"", idx)) != -1) {
                int colon = body.indexOf(':', idx);
                int start = body.indexOf('"', colon + 1) + 1;
                int end   = body.indexOf('"', start);
                if (start > 0 && end > start) {
                    if (messages.length() > 0) messages.append(", ");
                    messages.append(body, start, end);
                }
                idx = end + 1;
            }
            if (messages.length() == 0) {
                idx = 0;
                while ((idx = body.indexOf("\"title\"", idx)) != -1) {
                    int colon = body.indexOf(':', idx);
                    int start = body.indexOf('"', colon + 1) + 1;
                    int end   = body.indexOf('"', start);
                    if (start > 0 && end > start) {
                        if (messages.length() > 0) messages.append(", ");
                        messages.append(body, start, end);
                    }
                    idx = end + 1;
                }
            }
            return messages.length() > 0 ? messages.toString() : response.toString();
        } catch (Exception e) {
            return response.toString();
        }
    }
}
