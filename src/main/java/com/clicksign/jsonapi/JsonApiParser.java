package com.clicksign.jsonapi;

import java.util.*;

/**
 * Minimal JSON:API v1.1 response parser — zero external dependencies.
 *
 * <p>Parses {@code data} (single object or array), {@code included}, and {@code links}.
 * Attribute values are exposed as raw strings; callers cast as needed.
 */
public final class JsonApiParser {

    private JsonApiParser() {}

    public static ParsedResponse parse(String json) {
        if (json == null || json.isBlank()) {
            return new ParsedResponse(Collections.emptyList(), Collections.emptyList(), null);
        }
        return MinimalJsonParser.parseResponse(json);
    }

    public static final class ParsedResponse {

        private final List<ResourceObject> data;
        private final List<ResourceObject> included;
        private final String nextLink;

        public ParsedResponse(List<ResourceObject> data, List<ResourceObject> included,
                String nextLink) {
            this.data = Collections.unmodifiableList(data);
            this.included = Collections.unmodifiableList(included);
            this.nextLink = nextLink;
        }

        public List<ResourceObject> data() {
            return data;
        }

        public List<ResourceObject> included() {
            return included;
        }

        public String nextLink() {
            return nextLink;
        }

        /** Returns the first resource object, or throws if the response data is empty. */
        public ResourceObject firstData() {
            if (data.isEmpty()) {
                throw new IllegalStateException("API returned empty data");
            }
            return data.get(0);
        }
    }

    public static final class ResourceObject {

        private final String id;
        private final String type;
        private final Map<String, Object> attributes;
        private final Map<String, Object> relationships;

        public ResourceObject(String id, String type,
                Map<String, Object> attributes,
                Map<String, Object> relationships) {
            this.id = id;
            this.type = type;
            this.attributes = Collections.unmodifiableMap(attributes);
            this.relationships = Collections.unmodifiableMap(relationships);
        }

        public String id() {
            return id;
        }

        public String type() {
            return type;
        }

        public Map<String, Object> attributes() {
            return attributes;
        }

        public Map<String, Object> relationships() {
            return relationships;
        }

        @SuppressWarnings("unchecked")
        public String relationshipId(String name) {
            Object rel = relationships.get(name);
            if (!(rel instanceof Map)) {
                return null;
            }
            Object data = ((Map<String, Object>) rel).get("data");
            if (!(data instanceof Map)) {
                return null;
            }
            Object id = ((Map<String, Object>) data).get("id");
            return id != null ? id.toString() : null;
        }
    }
}
