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

    public String id() { return id; }

    public Map<String, Object> fields() { return fields; }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>(fields);
        m.put("id", id);
        return m;
    }

    public static DocumentTemplate of(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("templateId is required");
        }
        return new DocumentTemplate(templateId, null);
    }

    public static DocumentTemplate withFields(String templateId, Map<String, Object> fields) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("templateId is required");
        }
        return new DocumentTemplate(templateId, fields);
    }
}
