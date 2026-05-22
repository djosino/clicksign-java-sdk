package com.clicksign.jsonapi;

import java.util.Map;

/** Builds JSON:API request bodies without external dependencies. */
public final class JsonApiSerializer {

    private JsonApiSerializer() {}

    /**
     * Serializes a JSON:API request with type and attributes only.
     *
     * @param type       resource type
     * @param attributes attribute map
     * @return JSON string
     */
    public static String dump(String type, Map<String, Object> attributes) {
        return dump(type, null, attributes, null);
    }

    /**
     * Serializes a full JSON:API request body.
     *
     * @param type          resource type
     * @param id            resource id, or {@code null} for create
     * @param attributes    attribute map
     * @param relationships relationship map
     * @return JSON string
     */
    public static String dump(String type, String id,
            Map<String, Object> attributes,
            Map<String, Object> relationships) {
        StringBuilder sb = new StringBuilder("{\"data\":{");
        sb.append("\"type\":").append(quote(type));
        if (id != null) {
            sb.append(",\"id\":").append(quote(id));
        }
        if (attributes != null && !attributes.isEmpty()) {
            sb.append(",\"attributes\":").append(toJson(attributes));
        }
        if (relationships != null && !relationships.isEmpty()) {
            sb.append(",\"relationships\":").append(toJson(relationships));
        }
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Converts an arbitrary value to its JSON representation.
     *
     * @param value value to serialize
     * @return JSON string
     */
    @SuppressWarnings("unchecked")
    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return quote((String) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            ((Map<String, Object>) value).forEach((k, v) -> {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(quote(k)).append(':').append(toJson(v));
            });
            return sb.append('}').toString();
        }
        if (value instanceof Iterable) {
            StringBuilder sb = new StringBuilder("[");
            for (Object item : (Iterable<?>) value) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(toJson(item));
            }
            return sb.append(']').toString();
        }
        return quote(value.toString());
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
