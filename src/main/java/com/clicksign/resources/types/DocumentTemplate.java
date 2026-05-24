package com.clicksign.resources.types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Template reference when creating a document from a model. */
public final class DocumentTemplate {

    private final String id;
    private final Map<String, Object> fields;

    private DocumentTemplate(String id, Map<String, Object> fields) {
        this.id     = id;
        this.fields = fields != null
            ? Collections.unmodifiableMap(new LinkedHashMap<>(fields)) : Collections.emptyMap();
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
     * Returns the fields map.
     *
     * @return fields
     */
    public Map<String, Object> fields() {
        return fields;
    }

    /**
     * Serializes to an API-compatible map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>(fields);
        m.put("id", id);
        return Collections.unmodifiableMap(m);
    }

    /**
     * Creates a template reference with the given id and no fields.
     *
     * @param templateId template id
     * @return new instance
     * @throws IllegalArgumentException if templateId is blank
     */
    public static DocumentTemplate of(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("templateId is required");
        }
        return new DocumentTemplate(templateId, null);
    }

    /**
     * Creates a template reference with the given id and field values.
     *
     * @param templateId template id
     * @param fields     template field values
     * @return new instance
     * @throws IllegalArgumentException if templateId is blank
     */
    public static DocumentTemplate withFields(String templateId, Map<String, Object> fields) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("templateId is required");
        }
        return new DocumentTemplate(templateId, fields);
    }
}
