package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a Clicksign user. */
public final class User {

    private final String id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String createdAt;
    private final String modifiedAt;

    private User(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id          = obj.id();
        this.name        = str(a.get("name"));
        this.email       = str(a.get("email"));
        this.phoneNumber = str(a.get("phone_number"));
        this.createdAt   = str(a.get("created"));
        this.modifiedAt  = str(a.get("modified"));
    }

    /**
     * Returns the id.
     *
     * @return id
     */
    public String id() {
        return id;
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
     * Returns the phone number.
     *
     * @return phone number
     */
    public String phoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the created at timestamp.
     *
     * @return created at
     */
    public String createdAt() {
        return createdAt;
    }

    /**
     * Returns the modified at timestamp.
     *
     * @return modified at
     */
    public String modifiedAt() {
        return modifiedAt;
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for User operations. */
    public static final class Service {

        private static final String ENDPOINT = "/users";
        private final HttpClient http;

        /**
         * Constructs service.
         *
         * @param http HTTP client
         */
        public Service(HttpClient http) {
            this.http = http;
        }

        /**
         * Lists all resources.
         *
         * @return unmodifiable list
         */
        public List<User> list() {
            String raw = http.get(ENDPOINT, Collections.emptyMap());
            List<User> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new User(obj));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id resource id
         * @return resource
         */
        public User retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new User(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Retrieves the current user.
         *
         * @return current user
         */
        public User me() {
            String raw = http.get(ENDPOINT + "/me", Collections.emptyMap());
            return new User(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public User create(CreateParams params) {
            String body = JsonApiSerializer.dump("users", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new User(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating a user. */
    public static final class CreateParams {

        private final String name;
        private final String email;
        private final String phoneNumber;

        private CreateParams(Builder b) {
            this.name        = b.name;
            this.email       = b.email;
            this.phoneNumber = b.phoneNumber;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name",  name);
            m.put("email", email);
            if (phoneNumber != null) {
                m.put("phone_number", phoneNumber);
            }
            return m;
        }

        /**
         * Returns a new builder.
         *
         * @return new builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link CreateParams}. */
        public static final class Builder {
            private String name;
            private String email;
            private String phoneNumber;

            private Builder() {}

            /**
             * Sets name.
             *
             * @param v value
             * @return this builder
             */
            public Builder name(String v) {
                this.name = v;
                return this;
            }

            /**
             * Sets email.
             *
             * @param v value
             * @return this builder
             */
            public Builder email(String v) {
                this.email = v;
                return this;
            }

            /**
             * Sets phone number.
             *
             * @param v value
             * @return this builder
             */
            public Builder phoneNumber(String v) {
                this.phoneNumber = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("name is required");
                }
                if (email == null || email.isBlank()) {
                    throw new IllegalArgumentException("email is required");
                }
                return new CreateParams(this);
            }
        }
    }
}
