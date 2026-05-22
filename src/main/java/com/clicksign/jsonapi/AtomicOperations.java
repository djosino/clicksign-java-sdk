package com.clicksign.jsonapi;

import java.util.*;

/**
 * Builder for JSON:API Atomic Operations payload.
 *
 * <pre>{@code
 * AtomicOperations ops = new AtomicOperations();
 * ops.add(Map.of("type", "requirements", "attributes", ...));
 * ops.remove(Map.of("type", "requirements", "id", requirementId));
 * String json = ops.toJson();
 * }</pre>
 */
public final class AtomicOperations {

    private final List<Map<String, Object>> entries = new ArrayList<>();

    public AtomicOperations add(Map<String, Object> data) {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("op", "add");
        op.put("data", stringify(data));
        entries.add(op);
        return this;
    }

    public AtomicOperations remove(Map<String, Object> ref) {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("op", "remove");
        op.put("ref", stringify(ref));
        entries.add(op);
        return this;
    }

    public List<Map<String, Object>> entries() {
        return Collections.unmodifiableList(entries);
    }

    public String toJson() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("atomic:operations", entries);
        return JsonApiSerializer.toJson(root);
    }

    @SuppressWarnings("unchecked")
    private static Object stringify(Object value) {
        if (value instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<>();
            ((Map<Object, Object>) value).forEach((k, v) -> result.put(k.toString(), stringify(v)));
            return result;
        }
        if (value instanceof List) {
            List<Object> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                result.add(stringify(item));
            }
            return result;
        }
        return value;
    }
}
