package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.resources.types.AutoSignatureSigner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Automatic signature authorization term for a signer. */
public final class AutoSignatureTerm {

    private final String id;
    private final String name;
    private final String email;
    private final String documentation;
    private final String birthday;
    private final String createdAt;
    private final String modifiedAt;

    private AutoSignatureTerm(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id            = obj.id();
        this.name          = str(a.get("name"));
        this.email         = str(a.get("email"));
        this.documentation = str(a.get("documentation"));
        this.birthday      = str(a.get("birthday"));
        this.createdAt     = str(a.get("created"));
        this.modifiedAt    = str(a.get("modified"));
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
     * Returns the createdAt.
     *
     * @return createdAt
     */
    public String createdAt() {
        return createdAt;
    }

    /**
     * Returns the modifiedAt.
     *
     * @return modifiedAt
     */
    public String modifiedAt() {
        return modifiedAt;
    }

    @Override
    public String toString() {
        return "AutoSignatureTerm{id='" + id + "', email='" + email + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for AutoSignatureTerm operations. */
    public static final class Service {

        private static final String ENDPOINT = "/auto_signature/terms";
        private final HttpClient http;

        /**
         * Constructs service with the given HTTP client.
         *
         * @param http HTTP client
         */
        public Service(HttpClient http) {
            this.http = http;
        }

        /**
         * Retrieves AutoSignatureTerm by id.
         *
         * @param id resource id
         * @return AutoSignatureTerm
         */
        public AutoSignatureTerm retrieve(String id) {
            String raw = http.get(ENDPOINT + "/" + id, Collections.emptyMap());
            return new AutoSignatureTerm(JsonApiParser.parse(raw).firstData());
        }

        /**
         * Creates AutoSignatureTerm.
         *
         * @param params creation parameters
         * @return created AutoSignatureTerm
         */
        public AutoSignatureTerm create(CreateParams params) {
            String body = JsonApiSerializer.dump("auto_signature_terms", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new AutoSignatureTerm(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating an AutoSignatureTerm. */
    public static final class CreateParams {

        private final String signerName;
        private final String signerEmail;
        private final String signerDocumentation;
        private final String signerBirthday;
        private final String apiEmail;
        private final String adminEmail;

        private CreateParams(Builder b) {
            this.signerName          = b.signerName;
            this.signerEmail         = b.signerEmail;
            this.signerDocumentation = b.signerDocumentation;
            this.signerBirthday      = b.signerBirthday;
            this.apiEmail            = b.apiEmail;
            this.adminEmail          = b.adminEmail;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("signer", AutoSignatureSigner.builder()
                .name(signerName)
                .email(signerEmail)
                .documentation(signerDocumentation)
                .birthday(signerBirthday)
                .build()
                .toMap());
            m.put("api_email", apiEmail);
            m.put("admin_email", adminEmail);
            return m;
        }

        /**
         * Returns a new {@link Builder} for CreateParams.
         *
         * @return builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link CreateParams}. */
        public static final class Builder {
            private String signerName;
            private String signerEmail;
            private String signerDocumentation;
            private String signerBirthday;
            private String apiEmail;
            private String adminEmail;

            private Builder() {}

            /**
             * Sets all signer fields from an {@link AutoSignatureSigner}.
             *
             * @param signer signer value object
             * @return this
             */
            public Builder signer(AutoSignatureSigner signer) {
                return signerName(signer.name())
                    .signerEmail(signer.email())
                    .signerDocumentation(signer.documentation())
                    .signerBirthday(signer.birthday());
            }

            /**
             * Sets signerName.
             *
             * @param v value
             * @return this
             */
            public Builder signerName(String v) {
                this.signerName = v;
                return this;
            }

            /**
             * Sets signerEmail.
             *
             * @param v value
             * @return this
             */
            public Builder signerEmail(String v) {
                this.signerEmail = v;
                return this;
            }

            /**
             * Sets signerDocumentation.
             *
             * @param v value
             * @return this
             */
            public Builder signerDocumentation(String v) {
                this.signerDocumentation = v;
                return this;
            }

            /**
             * Sets signerBirthday.
             *
             * @param v value
             * @return this
             */
            public Builder signerBirthday(String v) {
                this.signerBirthday = v;
                return this;
            }

            /**
             * Sets apiEmail.
             *
             * @param v value
             * @return this
             */
            public Builder apiEmail(String v) {
                this.apiEmail = v;
                return this;
            }

            /**
             * Sets adminEmail.
             *
             * @param v value
             * @return this
             */
            public Builder adminEmail(String v) {
                this.adminEmail = v;
                return this;
            }

            /**
             * Builds params, validating required fields.
             *
             * @return new params
             * @throws IllegalArgumentException if required field is missing
             */
            public CreateParams build() {
                if (signerName == null || signerName.isBlank()) {
                    throw new IllegalArgumentException("signerName is required");
                }
                if (signerEmail == null || signerEmail.isBlank()) {
                    throw new IllegalArgumentException("signerEmail is required");
                }
                if (signerDocumentation == null || signerDocumentation.isBlank()) {
                    throw new IllegalArgumentException("signerDocumentation is required");
                }
                if (signerBirthday == null || signerBirthday.isBlank()) {
                    throw new IllegalArgumentException("signerBirthday is required");
                }
                if (apiEmail == null || apiEmail.isBlank()) {
                    throw new IllegalArgumentException("apiEmail is required");
                }
                if (adminEmail == null || adminEmail.isBlank()) {
                    throw new IllegalArgumentException("adminEmail is required");
                }
                return new CreateParams(this);
            }
        }
    }
}
