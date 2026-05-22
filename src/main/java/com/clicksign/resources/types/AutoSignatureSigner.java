package com.clicksign.resources.types;

import java.util.LinkedHashMap;
import java.util.Map;

/** Signer block inside automatic signature term creation. */
public final class AutoSignatureSigner {

    private final String name;
    private final String email;
    private final String documentation;
    private final String birthday;

    private AutoSignatureSigner(Builder b) {
        this.name          = b.name;
        this.email         = b.email;
        this.documentation = b.documentation;
        this.birthday      = b.birthday;
    }

    /**
     * Returns the name.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the email.
     *
     * @return email
     */
    public String email() {
        return email;
    }

    /**
     * Returns the documentation.
     *
     * @return documentation
     */
    public String documentation() {
        return documentation;
    }

    /**
     * Returns the birthday.
     *
     * @return birthday
     */
    public String birthday() {
        return birthday;
    }

    /**
     * Returns a map representation suitable for JSON serialization.
     *
     * @return attribute map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("email", email);
        m.put("documentation", documentation);
        m.put("birthday", birthday);
        return m;
    }

    /**
     * Returns a new {@link Builder} for AutoSignatureSigner.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link AutoSignatureSigner}. */
    public static final class Builder {

        /** Creates a new builder. */
        public Builder() {}

        private String name;
        private String email;
        private String documentation;
        private String birthday;

        /**
         * Sets name.
         *
         * @param v value
         * @return this
         */
        public Builder name(String v) {
            this.name = v;
            return this;
        }

        /**
         * Sets email.
         *
         * @param v value
         * @return this
         */
        public Builder email(String v) {
            this.email = v;
            return this;
        }

        /**
         * Sets documentation.
         *
         * @param v value
         * @return this
         */
        public Builder documentation(String v) {
            this.documentation = v;
            return this;
        }

        /**
         * Sets birthday.
         *
         * @param v value
         * @return this
         */
        public Builder birthday(String v) {
            this.birthday = v;
            return this;
        }

        /**
         * Builds params, validating required fields.
         *
         * @return new AutoSignatureSigner
         * @throws IllegalArgumentException if required field is missing
         */
        public AutoSignatureSigner build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("email is required");
            }
            if (documentation == null || documentation.isBlank()) {
                throw new IllegalArgumentException("documentation is required");
            }
            if (birthday == null || birthday.isBlank()) {
                throw new IllegalArgumentException("birthday is required");
            }
            return new AutoSignatureSigner(this);
        }
    }
}
