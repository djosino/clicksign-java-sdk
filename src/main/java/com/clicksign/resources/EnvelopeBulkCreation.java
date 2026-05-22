package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents an asynchronous envelope bulk creation job. */
public final class EnvelopeBulkCreation {

    private final String id;
    private final String jobId;
    private final String enqueuedAt;

    private EnvelopeBulkCreation(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id          = obj.id();
        this.jobId       = str(a.get("job_id"));
        this.enqueuedAt  = str(a.get("enqueued_at"));
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
     * Returns the job id.
     *
     * @return job id
     */
    public String jobId() {
        return jobId;
    }

    /**
     * Returns the enqueued at timestamp.
     *
     * @return enqueued at
     */
    public String enqueuedAt() {
        return enqueuedAt;
    }

    @Override
    public String toString() {
        return "EnvelopeBulkCreation{id='" + id + "', jobId='" + jobId + "'}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for EnvelopeBulkCreation operations. */
    public static final class Service {

        private static final String ENDPOINT = "/envelope_bulk_creations";
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
         * Creates resource.
         *
         * @param params creation parameters
         * @return created resource
         */
        public EnvelopeBulkCreation create(CreateParams params) {
            String body = JsonApiSerializer.dump("envelope_bulk_creations", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new EnvelopeBulkCreation(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    /** Parameters for creating an envelope bulk creation job. */
    public static final class CreateParams {

        private final Map<String, Object> envelope;
        private final Map<String, Object> document;
        private final List<Map<String, Object>> signers;

        private CreateParams(Builder b) {
            this.envelope = Collections.unmodifiableMap(new LinkedHashMap<>(b.envelope));
            this.document = Collections.unmodifiableMap(new LinkedHashMap<>(b.document));
            this.signers  = Collections.unmodifiableList(new ArrayList<>(b.signers));
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("envelope", envelope);
            m.put("document", document);
            m.put("signers",  signers);
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
            private Map<String, Object> envelope;
            private Map<String, Object> document;
            private List<Map<String, Object>> signers = new ArrayList<>();

            private Builder() {}

            /**
             * Sets envelope attributes.
             *
             * @param v value
             * @return this builder
             */
            public Builder envelope(Map<String, Object> v) {
                this.envelope = v;
                return this;
            }

            /**
             * Sets document attributes.
             *
             * @param v value
             * @return this builder
             */
            public Builder document(Map<String, Object> v) {
                this.document = v;
                return this;
            }

            /**
             * Sets the signers list.
             *
             * @param v value
             * @return this builder
             */
            public Builder signers(List<Map<String, Object>> v) {
                this.signers = new ArrayList<>(v);
                return this;
            }

            /**
             * Adds a signer to the list.
             *
             * @param v signer attributes
             * @return this builder
             */
            public Builder addSigner(Map<String, Object> v) {
                this.signers.add(v);
                return this;
            }

            /**
             * Builds and validates.
             *
             * @return new instance
             * @throws IllegalArgumentException if required fields are missing
             */
            public CreateParams build() {
                if (envelope == null) {
                    throw new IllegalArgumentException("envelope is required");
                }
                if (document == null) {
                    throw new IllegalArgumentException("document is required");
                }
                if (signers.isEmpty()) {
                    throw new IllegalArgumentException("at least one signer is required");
                }
                return new CreateParams(this);
            }
        }
    }
}
