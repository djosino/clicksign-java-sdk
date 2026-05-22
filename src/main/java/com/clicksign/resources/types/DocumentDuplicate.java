package com.clicksign.resources.types;

import java.util.Map;

/** Reference to an existing document when duplicating into an envelope. */
public final class DocumentDuplicate {

    private final String id;

    private DocumentDuplicate(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Map<String, Object> toMap() {
        return Map.of("id", id);
    }

    public static DocumentDuplicate of(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("documentId is required");
        }
        return new DocumentDuplicate(documentId);
    }
}
