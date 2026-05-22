package com.clicksign.resources.types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Email customization block for notification requests. */
public final class EmailCustomization {

    private final String subject;

    private EmailCustomization(Builder b) {
        this.subject = b.subject;
    }

    /**
     * Returns the subject.
     *
     * @return subject
     */
    public String subject() {
        return subject;
    }

    /**
     * Serializes to an API-compatible map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (subject != null) {
            m.put("subject", subject);
        }
        return Collections.unmodifiableMap(m);
    }

    /**
     * Constructs from a raw API map.
     *
     * @param raw map from JSON:API attributes
     * @return new instance, or {@code null} if the map is empty
     */
    public static EmailCustomization fromMap(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        Object subject = raw.get("subject");
        return builder()
            .subject(subject != null ? subject.toString() : null)
            .build();
    }

    /**
     * Returns a new builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link EmailCustomization}. */
    public static final class Builder {
        private String subject;

        /** Creates a new instance. */
        public Builder() {}

        /**
         * Sets subject.
         *
         * @param v value
         * @return this builder
         */
        public Builder subject(String v) {
            this.subject = v;
            return this;
        }

        /**
         * Builds and validates.
         *
         * @return new instance
         */
        public EmailCustomization build() {
            return new EmailCustomization(this);
        }
    }
}
