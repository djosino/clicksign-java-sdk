package com.clicksign.resources.types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Typed wrapper for JSON:API {@code metadata} objects. */
public final class Metadata {

    private final Map<String, Object> values;

    private Metadata(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static Metadata empty() {
        return new Metadata(Collections.emptyMap());
    }

    public static Metadata of(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return new Metadata(m);
    }

    public static Metadata fromMap(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;
        return new Metadata(raw);
    }

    public Map<String, Object> toMap() {
        return values;
    }

    public Object get(String key) {
        return values.get(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Builder toBuilder() {
        Builder b = builder();
        values.forEach(b::put);
        return b;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Object> values = new LinkedHashMap<>();

        public Builder put(String key, Object value) {
            values.put(Objects.requireNonNull(key), value);
            return this;
        }

        public Metadata build() {
            return new Metadata(values);
        }
    }
}
