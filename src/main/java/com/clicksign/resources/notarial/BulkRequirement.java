package com.clicksign.resources.notarial;

import com.clicksign.errors.ValidationException;
import com.clicksign.jsonapi.AtomicOperations;
import com.clicksign.jsonapi.BulkOperationsClient;
import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RubricateKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Atomic bulk operations on signing requirements.
 *
 * <pre>{@code
 * BulkRequirement.Response response = client.bulkRequirements().create(
 *     envelopeId,
 *     ops -> ops
 *         .addAgree(signerId, documentId, "sign")
 *         .addProvideEvidence(signerId, documentId, "email")
 * );
 * if (response.isSuccess()) {
 *     response.requirements().forEach(r -> System.out.println(r.id()));
 * }
 * }</pre>
 */
public final class BulkRequirement {

    private BulkRequirement() {}

    // ── Operations builder ───────────────────────────────────────────────────

    /** Builder for atomic bulk operations on signing requirements. */
    public static final class Operations {

        /** Creates a new empty operations builder. */
        public Operations() {}

        private final AtomicOperations atomicOps = new AtomicOperations();

        /**
         * Adds an agree operation for the signer on the document.
         *
         * @param signerId signer id
         * @param documentId document id
         * @param role signing role (e.g. "sign")
         * @return this operations builder
         */
        public Operations addAgree(String signerId, String documentId, String role) {
            validateIds(signerId, documentId);
            if (role == null || role.isBlank()) {
                throw new IllegalArgumentException("role is required");
            }
            atomicOps.add(buildData(signerId, documentId, Map.of("action", "agree", "role", role)));
            return this;
        }

        /**
         * Adds an agree operation using a typed role enum.
         *
         * @param signerId signer id
         * @param documentId document id
         * @param role signing role
         * @return this operations builder
         */
        public Operations addAgree(String signerId, String documentId, RequirementRole role) {
            return addAgree(signerId, documentId, role != null ? role.apiValue() : null);
        }

        /**
         * Adds a provide_evidence operation for the signer on the document.
         *
         * @param signerId signer id
         * @param documentId document id
         * @param auth authentication method (e.g. "email")
         * @return this operations builder
         */
        public Operations addProvideEvidence(String signerId, String documentId, String auth) {
            validateIds(signerId, documentId);
            if (auth == null || auth.isBlank()) {
                throw new IllegalArgumentException("auth is required");
            }
            atomicOps.add(buildData(signerId, documentId, Map.of("action", "provide_evidence", "auth", auth)));
            return this;
        }

        /**
         * Adds a provide_evidence operation using a typed auth enum.
         *
         * @param signerId signer id
         * @param documentId document id
         * @param auth authentication method
         * @return this operations builder
         */
        public Operations addProvideEvidence(String signerId, String documentId, RequirementAuth auth) {
            return addProvideEvidence(signerId, documentId, auth != null ? auth.apiValue() : null);
        }

        /**
         * Adds a rubricate operation for the signer on the document.
         *
         * @param signerId signer id
         * @param documentId document id
         * @param pages comma-separated page numbers, or null
         * @param rubricField rubric field identifier, or null
         * @param kind rubricate kind, or null
         * @return this operations builder
         */
        public Operations addRubricate(String signerId, String documentId,
                                        String pages, String rubricField, String kind) {
            validateIds(signerId, documentId);
            if (pages == null && rubricField == null) {
                throw new IllegalArgumentException("pages or rubricField is required");
            }
            if (kind != null && ApiStringEnum.tryParse(RubricateKind.class, kind) == null) {
                throw new IllegalArgumentException("kind must be one of: initials, manuscript");
            }
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("action", "rubricate");
            if (pages       != null) {
                attrs.put("pages",        pages);
            }
            if (rubricField != null) {
                attrs.put("rubric_field", rubricField);
            }
            if (kind        != null) {
                attrs.put("kind",         kind);
            }
            atomicOps.add(buildData(signerId, documentId, attrs));
            return this;
        }

        /**
         * Removes (deletes) a requirement by id.
         *
         * @param requirementId requirement id to delete
         * @return this operations builder
         */
        public Operations remove(String requirementId) {
            if (requirementId == null || requirementId.isBlank()) {
                throw new IllegalArgumentException("requirementId is required");
            }
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("type", "requirements");
            ref.put("id",   requirementId);
            atomicOps.remove(ref);
            return this;
        }

        List<Map<String, Object>> entries() {
            return atomicOps.entries();
        }

        String toJson() {
            return atomicOps.toJson();
        }

        private static void validateIds(String signerId, String documentId) {
            if (signerId == null || signerId.isBlank()) {
                throw new IllegalArgumentException("signerId is required");
            }
            if (documentId == null || documentId.isBlank()) {
                throw new IllegalArgumentException("documentId is required");
            }
        }

        private static Map<String, Object> buildData(String signerId, String documentId,
                                                      Map<String, Object> attributes) {
            Map<String, Object> signerData = new LinkedHashMap<>();
            signerData.put("type", "signers");
            signerData.put("id",   signerId);
            Map<String, Object> signerRel = new LinkedHashMap<>();
            signerRel.put("data", signerData);

            Map<String, Object> documentData = new LinkedHashMap<>();
            documentData.put("type", "documents");
            documentData.put("id",   documentId);
            Map<String, Object> documentRel = new LinkedHashMap<>();
            documentRel.put("data", documentData);

            Map<String, Object> rels = new LinkedHashMap<>();
            rels.put("signer",   signerRel);
            rels.put("document", documentRel);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type",          "requirements");
            data.put("attributes",    attributes);
            data.put("relationships", rels);
            return data;
        }
    }

    // ── Response ─────────────────────────────────────────────────────────────

    /** Result of a bulk requirement operation, containing per-operation results. */
    public static final class Response {

        private final String envelopeId;
        private final List<OperationResult> results;

        private Response(String envelopeId, List<OperationResult> results) {
            this.envelopeId = envelopeId;
            this.results    = Collections.unmodifiableList(results);
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
         * Returns the results.
         *
         * @return results
         */
        public List<OperationResult> results() {
            return results;
        }

        /**
         * Returns the is success.
         *
         * @return is success
         */
        public boolean isSuccess() {
            for (OperationResult r : results) {
                if (!r.isSuccess()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Returns the requirements.
         *
         * @return requirements
         */
        public List<Requirement> requirements() {
            List<Requirement> list = new ArrayList<>();
            for (OperationResult r : results) {
                if (r.requirement() != null) {
                    list.add(r.requirement());
                }
            }
            return Collections.unmodifiableList(list);
        }

        /**
         * Returns the failures.
         *
         * @return failures
         */
        public List<OperationResult> failures() {
            List<OperationResult> list = new ArrayList<>();
            for (OperationResult r : results) {
                if (!r.isSuccess()) {
                    list.add(r);
                }
            }
            return Collections.unmodifiableList(list);
        }
    }

    // ── OperationResult ───────────────────────────────────────────────────────

    /** Result of a single atomic operation within a bulk request. */
    public static final class OperationResult {

        private final int index;
        private final String op;
        private final Requirement requirement;
        private final List<Map<String, Object>> errors;

        private OperationResult(int index, String op, Requirement requirement,
                                 List<Map<String, Object>> errors) {
            this.index       = index;
            this.op          = op;
            this.requirement = requirement;
            this.errors      = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
        }

        /**
         * Returns the zero-based index of this operation in the batch.
         *
         * @return operation index
         */
        public int index() {
            return index;
        }

        /**
         * Returns the operation type (e.g. {@code "add"} or {@code "remove"}).
         *
         * @return operation type
         */
        public String op() {
            return op;
        }

        /**
         * Returns the resulting {@link Requirement}, or {@code null} if the operation failed.
         *
         * @return requirement or null
         */
        public Requirement requirement() {
            return requirement;
        }

        /**
         * Returns the list of error objects for this operation, empty if successful.
         *
         * @return list of error maps
         */
        public List<Map<String, Object>> errors() {
            return errors;
        }

        /**
         * Returns {@code true} if this operation produced no errors.
         *
         * @return true if successful
         */
        public boolean isSuccess() {
            return errors.isEmpty();
        }
    }

    // ── Service ─────────────────────────────────────────────────────────────

    /** HTTP service for BulkRequirement operations. */
    public static final class Service {

        private final BulkOperationsClient bulkClient;

        /**
         * Constructs service with the given bulk HTTP client.
         *
         * @param bulkClient bulk operations HTTP client
         */
        public Service(BulkOperationsClient bulkClient) {
            this.bulkClient = bulkClient;
        }

        /** Functional interface for building bulk operations inline. */
        @FunctionalInterface
        public interface OperationsBuilder {
            /**
             * Builds the operations list.
             *
             * @param ops operations accumulator
             * @return populated operations
             */
            Operations build(Operations ops);
        }

        /**
         * Creates bulk requirements via atomic operations.
         *
         * @param envelopeId envelope id
         * @param builder operations builder lambda
         * @return bulk operation response
         */
        public Response create(String envelopeId, OperationsBuilder builder) {
            Operations ops = builder.build(new Operations());
            String responseBody = bulkClient.post(
                "/envelopes/" + envelopeId + "/bulk_requirements", ops.toJson());
            return parseResponse(responseBody, envelopeId, ops.entries());
        }

        @SuppressWarnings("unchecked")
        private static Response parseResponse(String json, String envelopeId,
                                               List<Map<String, Object>> opEntries) {
            Map<String, Object> root = com.clicksign.jsonapi.MinimalJsonParser.parseObject(json);

            if (root.containsKey("errors") && !root.containsKey("atomic:results")) {
                Object errorsObj = root.get("errors");
                String message = "Validation failed";
                if (errorsObj instanceof List) {
                    StringBuilder sb = new StringBuilder();
                    for (Object e : (List<?>) errorsObj) {
                        if (e instanceof Map) {
                            Object detail = ((Map<String, Object>) e).get("detail");
                            if (detail == null) {
                                detail = ((Map<String, Object>) e).get("title");
                            }
                            if (detail != null) {
                                if (sb.length() > 0) {
                                    sb.append(", ");
                                }
                                sb.append(detail);
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        message = sb.toString();
                    }
                }
                throw new ValidationException(message, 422, null, json);
            }

            List<OperationResult> results = new ArrayList<>();
            Object rawResults = root.get("atomic:results");
            if (rawResults instanceof List) {
                List<?> slots = (List<?>) rawResults;
                for (int i = 0; i < slots.size(); i++) {
                    Object slot = slots.get(i);
                    if (!(slot instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> slotMap = (Map<String, Object>) slot;
                    String op = (opEntries.size() > i) ? str(opEntries.get(i).get("op")) : null;

                    List<Map<String, Object>> errors = null;
                    if (slotMap.containsKey("errors") && slotMap.get("errors") instanceof List) {
                        errors = (List<Map<String, Object>>) slotMap.get("errors");
                    }

                    Requirement req = null;
                    Object data = slotMap.get("data");
                    if (data instanceof Map && !((Map<?, ?>) data).isEmpty()) {
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        Map<String, Object> attrs = dataMap.get("attributes") instanceof Map
                            ? (Map<String, Object>) dataMap.get("attributes") : Collections.emptyMap();
                        Map<String, Object> rels = dataMap.get("relationships") instanceof Map
                            ? (Map<String, Object>) dataMap.get("relationships") : Collections.emptyMap();
                        com.clicksign.jsonapi.JsonApiParser.ResourceObject obj =
                            new com.clicksign.jsonapi.JsonApiParser.ResourceObject(
                                str(dataMap.get("id")), str(dataMap.get("type")), attrs, rels);
                        req = Requirement.fromResource(obj, envelopeId);
                    }

                    results.add(new OperationResult(i, op, req, errors));
                }
            }
            return new Response(envelopeId, results);
        }

        private static String str(Object o) {
            return o != null ? o.toString() : null;
        }
    }
}
