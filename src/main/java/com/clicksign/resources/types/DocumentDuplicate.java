package com.clicksign.resources.types;

import java.util.Map;

/** Reference to an existing document when duplicating into an envelope. */
public final class DocumentDuplicate {

    private final String id;

    private DocumentDuplicate(String id) {
        this.id = id;
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
     * Serializes to an API-compatible map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        return Map.of("id", id);
    }

    /**
     * Creates a new instance with the given document id.
     *
     * @param documentId document id
     * @return new instance
     * @throws IllegalArgumentException if documentId is blank
     */
    public static DocumentDuplicate of(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("documentId is required");
        }
        return new DocumentDuplicate(documentId);
    }
}
