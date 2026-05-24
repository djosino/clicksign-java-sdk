package com.clicksign.resources.notarial;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.jsonapi.SignerQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a Clicksign signer. Signers do not support update. */
public final class Signer {

    private final String id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String birthday;
    private final boolean refusable;
    private final boolean locationRequiredEnabled;
    private final boolean hasDocumentation;
    private final String documentation;
    private final SignatureHost signatureHost;
    private final Map<String, Object> communicateEvents;
    private final Integer group;
    private final String envelopeId;
    private final String createdAt;
    private final String modifiedAt;

    /**
     * Constructs from a JSON:API resource object.
     *
     * @param obj              resource object
     * @param parentEnvelopeId envelope id
     * @return new instance
     */
    @SuppressWarnings("unchecked")
    public static Signer from(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        return new Signer(obj, parentEnvelopeId);
    }

    private Signer(JsonApiParser.ResourceObject obj, String parentEnvelopeId) {
        Map<String, Object> a = obj.attributes();
        this.id                      = obj.id();
        this.name                    = str(a.get("name"));
        this.email                   = str(a.get("email"));
        this.phoneNumber             = str(a.get("phone_number"));
        this.birthday                = str(a.get("birthday"));
        this.refusable               = bool(a.get("refusable"));
        this.locationRequiredEnabled = bool(a.get("location_required_enabled"));
        this.hasDocumentation        = bool(a.get("has_documentation"));
        this.documentation           = str(a.get("documentation"));
        this.signatureHost           = parseSignatureHost(a.get("signature_host"));
        this.communicateEvents       = objectMap(a.get("communicate_events"));
        this.group                   = integer(a.get("group"));
        this.envelopeId              = parentEnvelopeId != null ? parentEnvelopeId : obj.relationshipId("envelope");
        this.createdAt               = str(a.get("created"));
        this.modifiedAt              = str(a.get("modified"));
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
     * Returns the birthday.
     *
     * @return birthday
     */
    public String birthday() {
        return birthday;
    }

    /**
     * Returns whether the signer can refuse.
     *
     * @return refusable flag
     */
    public boolean refusable() {
        return refusable;
    }

    /**
     * Returns whether location is required.
     *
     * @return location required enabled flag
     */
    public boolean locationRequiredEnabled() {
        return locationRequiredEnabled;
    }

    /**
     * Returns whether the signer has documentation.
     *
     * @return has documentation flag
     */
    public boolean hasDocumentation() {
        return hasDocumentation;
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
     * Returns the signature host.
     *
     * @return signature host
     */
    public SignatureHost signatureHost() {
        return signatureHost;
    }

    /**
     * Returns the communicate events map.
     *
     * @return communicate events map
     */
    public Map<String, Object> communicateEvents() {
        return communicateEvents;
    }

    /**
     * Parsed view of {@link #communicateEvents()}; {@code null} when unset.
     *
     * @return parsed communicate events, or {@code null}
     */
    public CommunicateEvents communicateEventsConfig() {
        return CommunicateEvents.fromMap(communicateEvents);
    }

    /**
     * Returns the group number.
     *
     * @return group
     */
    public Integer group() {
        return group;
    }

    /**
     * Returns the envelope id.
     *
     * @return envelope id
     */
    public String envelopeId() {
        return envelopeId;
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
        return "Signer{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static boolean bool(Object o) {
        return Boolean.TRUE.equals(o) || "true".equals(str(o));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>((Map<String, Object>) o));
    }

    private static Integer integer(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static SignatureHost parseSignatureHost(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        Map<String, Object> m = (Map<String, Object>) o;
        return new SignatureHost(
            str(m.get("name")),
            str(m.get("email")),
            objectMap(m.get("communicate_events")));
    }

    /** Signature host with its own communication preferences. */
    public static final class SignatureHost {

        private final String name;
        private final String email;
        private final Map<String, Object> communicateEvents;

        /**
         * Constructs a signature host.
         *
         * @param name              host name
         * @param email             host email
         * @param communicateEvents communicate events map
         */
        public SignatureHost(String name, String email, Map<String, Object> communicateEvents) {
            this.name = name;
            this.email = email;
            this.communicateEvents = communicateEvents != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(communicateEvents)) : null;
        }

        /**
         * Constructs a signature host.
         *
         * @param name              host name
         * @param email             host email
         * @param communicateEvents communicate events configuration
         */
        public SignatureHost(String name, String email, CommunicateEvents communicateEvents) {
            this(name, email, communicateEvents != null ? communicateEvents.toMap() : null);
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
         * Returns the communicate events map.
         *
         * @return communicate events map
         */
        public Map<String, Object> communicateEvents() {
            return communicateEvents;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name  != null) {
                m.put("name",  name);
            }
            if (email != null) {
                m.put("email", email);
            }
            if (communicateEvents != null) {
                m.put("communicate_events", communicateEvents);
            }
            return m;
        }
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for Signer operations. */
    public static final class Service {

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
         * Lists all resources for the given envelope.
         *
         * @param envelopeId envelope id
         * @return unmodifiable list
         */
        public List<Signer> list(String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signers", Collections.emptyMap());
            List<Signer> result = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
                result.add(new Signer(obj, envelopeId));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Returns a fluent query builder for filtering and paginating signers.
         *
         * @param envelopeId envelope id to scope the query
         * @return query builder
         */
        public SignerQuery filter(String envelopeId) {
            return new SignerQuery(envelopeId, http);
        }

        /**
         * Retrieves resource by id.
         *
         * @param id         resource id
         * @param envelopeId envelope id
         * @return resource
         */
        public Signer retrieve(String id, String envelopeId) {
            String raw = http.get("/envelopes/" + envelopeId + "/signers/" + id, Collections.emptyMap());
            return new Signer(JsonApiParser.parse(raw).firstData(), envelopeId);
        }

        /**
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public Signer create(CreateParams params) {
            String body = JsonApiSerializer.dump("signers", null, params.toAttributes(), null);
            String raw  = http.post("/envelopes/" + params.envelopeId + "/signers", body);
            return new Signer(JsonApiParser.parse(raw).firstData(), params.envelopeId);
        }

        /**
         * Deletes resource by id.
         *
         * @param id         resource id
         * @param envelopeId envelope id
         */
        public void delete(String id, String envelopeId) {
            http.delete("/envelopes/" + envelopeId + "/signers/" + id, null);
        }

        /**
         * Sends a notification to a signer.
         *
         * @param id         signer id
         * @param envelopeId envelope id
         * @param params     notification parameters
         */
        public void notify(String id, String envelopeId, NotificationParams params) {
            String body = JsonApiSerializer.dump("notifications", null, params.toAttributes(), null);
            http.post("/envelopes/" + envelopeId + "/signers/" + id + "/notifications", body);
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating a signer. */
    public static final class CreateParams {

        private final String envelopeId;
        private final String name;
        private final String email;
        private final String phoneNumber;
        private final String birthday;
        private final Boolean refusable;
        private final Boolean locationRequiredEnabled;
        private final Boolean hasDocumentation;
        private final String documentation;
        private final SignatureHost signatureHost;
        private final Map<String, Object> communicateEvents;
        private final Integer group;

        private CreateParams(Builder b) {
            this.envelopeId              = b.envelopeId;
            this.name                    = b.name;
            this.email                   = b.email;
            this.phoneNumber             = b.phoneNumber;
            this.birthday                = b.birthday;
            this.refusable               = b.refusable;
            this.locationRequiredEnabled = b.locationRequiredEnabled;
            this.hasDocumentation        = b.hasDocumentation;
            this.documentation           = b.documentation;
            this.signatureHost           = b.signatureHost;
            this.communicateEvents       = b.communicateEvents != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(b.communicateEvents)) : null;
            this.group                   = b.group;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (name  != null) {
                m.put("name",  name);
            }
            if (email != null) {
                m.put("email", email);
            }
            if (phoneNumber != null) {
                m.put("phone_number", phoneNumber);
            }
            if (birthday != null) {
                m.put("birthday", birthday);
            }
            if (refusable != null) {
                m.put("refusable", refusable);
            }
            if (locationRequiredEnabled != null) {
                m.put("location_required_enabled", locationRequiredEnabled);
            }
            if (hasDocumentation != null) {
                m.put("has_documentation", hasDocumentation);
            }
            if (documentation != null) {
                m.put("documentation", documentation);
            }
            if (signatureHost != null) {
                m.put("signature_host", signatureHost.toAttributes());
            }
            if (communicateEvents != null) {
                m.put("communicate_events", communicateEvents);
            }
            if (group != null) {
                m.put("group", group);
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
            private String envelopeId;
            private String name;
            private String email;
            private String phoneNumber;
            private String birthday;
            private Boolean refusable;
            private Boolean locationRequiredEnabled;
            private Boolean hasDocumentation;
            private String documentation;
            private SignatureHost signatureHost;
            private Map<String, Object> communicateEvents;
            private Integer group;

            private Builder() {}

            /**
             * Sets envelope id.
             *
             * @param v value
             * @return this builder
             */
            public Builder envelopeId(String v) {
                this.envelopeId = v;
                return this;
            }

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
             * Sets birthday.
             *
             * @param v value
             * @return this builder
             */
            public Builder birthday(String v) {
                this.birthday = v;
                return this;
            }

            /**
             * Sets refusable flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder refusable(boolean v) {
                this.refusable = v;
                return this;
            }

            /**
             * Sets location required enabled flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder locationRequiredEnabled(boolean v) {
                this.locationRequiredEnabled = v;
                return this;
            }

            /**
             * Sets has documentation flag.
             *
             * @param v value
             * @return this builder
             */
            public Builder hasDocumentation(boolean v) {
                this.hasDocumentation = v;
                return this;
            }

            /**
             * Sets documentation.
             *
             * @param v value
             * @return this builder
             */
            public Builder documentation(String v) {
                this.documentation = v;
                return this;
            }

            /**
             * Sets signature host.
             *
             * @param v value
             * @return this builder
             */
            public Builder signatureHost(SignatureHost v) {
                this.signatureHost = v;
                return this;
            }

            /**
             * Sets communicate events.
             *
             * @param v value
             * @return this builder
             */
            public Builder communicateEvents(Map<String, Object> v) {
                this.communicateEvents = v;
                return this;
            }

            /**
             * Sets communicate events.
             *
             * @param v value
             * @return this builder
             */
            public Builder communicateEvents(CommunicateEvents v) {
                return communicateEvents(v != null ? v.toMap() : null);
            }

            /**
             * Sets group number.
             *
             * @param v value
             * @return this builder
             */
            public Builder group(int v) {
                this.group = v;
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (envelopeId == null || envelopeId.isBlank()) {
                    throw new IllegalArgumentException("envelopeId is required");
                }
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("name is required");
                }
                if (email == null || email.isBlank()) {
                    throw new IllegalArgumentException("email is required");
                }
                SignerValidation.validateName(name);
                SignerValidation.validateOptionalFields(
                    hasDocumentation, documentation, birthday, phoneNumber, communicateEvents);
                return new CreateParams(this);
            }
        }
    }

}
