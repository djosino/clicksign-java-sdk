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

    /**
     * Returns an empty metadata instance.
     *
     * @return empty instance
     */
    public static Metadata empty() {
        return new Metadata(Collections.emptyMap());
    }

    /**
     * Creates a metadata instance with a single key-value entry.
     *
     * @param key   entry key
     * @param value entry value
     * @return new instance
     */
    public static Metadata of(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return new Metadata(m);
    }

    /**
     * Constructs from a raw map.
     *
     * @param raw map from JSON:API attributes
     * @return new instance, or {@code null} if the map is empty
     */
    public static Metadata fromMap(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return new Metadata(raw);
    }

    /**
     * Returns the underlying map.
     *
     * @return values map
     */
    public Map<String, Object> toMap() {
        return values;
    }

    /**
     * Returns the value for the given key.
     *
     * @param key entry key
     * @return value, or {@code null} if absent
     */
    public Object get(String key) {
        return values.get(key);
    }

    /**
     * Returns whether this instance is empty.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Returns a new builder pre-populated with this instance's values.
     *
     * @return builder
     */
    public Builder toBuilder() {
        Builder b = builder();
        values.forEach(b::put);
        return b;
    }

    /**
     * Returns a new builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link Metadata}. */
    public static final class Builder {
        private final Map<String, Object> values = new LinkedHashMap<>();

        /** Creates a new instance. */
        public Builder() {}

        /**
         * Puts a key-value entry.
         *
         * @param key   entry key
         * @param value entry value
         * @return this builder
         */
        public Builder put(String key, Object value) {
            values.put(Objects.requireNonNull(key), value);
            return this;
        }

        /**
         * Builds and validates.
         *
         * @return new instance
         */
        public Metadata build() {
            return new Metadata(values);
        }
    }
}
