package com.clicksign.jsonapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON:API v1.1 response parser — zero external dependencies.
 *
 * <p>Parses {@code data} (single object or array), {@code included}, and {@code links}.
 * Attribute values are exposed as raw strings; callers cast as needed.
 */
public final class JsonApiParser {

    private JsonApiParser() {}

    /**
     * Parses a JSON:API response string.
     *
     * @param json JSON string
     * @return parsed response
     */
    public static ParsedResponse parse(String json) {
        if (json == null || json.isBlank()) {
            return new ParsedResponse(Collections.emptyList(), Collections.emptyList(), null);
        }
        return MinimalJsonParser.parseResponse(json);
    }

    /** Parsed JSON:API response containing data, included, and pagination links. */
    public static final class ParsedResponse {

        private final List<ResourceObject> data;
        private final List<ResourceObject> included;
        private final String nextLink;

        /**
         * Constructs a parsed response.
         *
         * @param data     primary data
         * @param included included resources
         * @param nextLink next page link, or {@code null}
         */
        public ParsedResponse(List<ResourceObject> data, List<ResourceObject> included,
                String nextLink) {
            this.data = Collections.unmodifiableList(data);
            this.included = Collections.unmodifiableList(included);
            this.nextLink = nextLink;
        }

        /**
         * Returns the primary data list.
         *
         * @return data
         */
        public List<ResourceObject> data() {
            return data;
        }

        /**
         * Returns the included resources.
         *
         * @return included
         */
        public List<ResourceObject> included() {
            return included;
        }

        /**
         * Returns the next page link.
         *
         * @return next link, or {@code null}
         */
        public String nextLink() {
            return nextLink;
        }

        /**
         * Returns the first resource object, or throws if the response data is empty.
         *
         * @return first resource object
         */
        public ResourceObject firstData() {
            if (data.isEmpty()) {
                throw new IllegalStateException("API returned empty data");
            }
            return data.get(0);
        }
    }

    /** A single JSON:API resource object with id, type, attributes and relationships. */
    public static final class ResourceObject {

        private final String id;
        private final String type;
        private final Map<String, Object> attributes;
        private final Map<String, Object> relationships;

        /**
         * Constructs a resource object.
         *
         * @param id            resource id
         * @param type          resource type
         * @param attributes    attribute map
         * @param relationships relationship map
         */
        public ResourceObject(String id, String type,
                Map<String, Object> attributes,
                Map<String, Object> relationships) {
            this.id = id;
            this.type = type;
            this.attributes = Collections.unmodifiableMap(attributes);
            this.relationships = Collections.unmodifiableMap(relationships);
        }

        /**
         * Returns the resource id.
         *
         * @return id
         */
        public String id() {
            return id;
        }

        /**
         * Returns the resource type.
         *
         * @return type
         */
        public String type() {
            return type;
        }

        /**
         * Returns the attribute map.
         *
         * @return attributes
         */
        public Map<String, Object> attributes() {
            return attributes;
        }

        /**
         * Returns the relationship map.
         *
         * @return relationships
         */
        public Map<String, Object> relationships() {
            return relationships;
        }

        /**
         * Extracts the id of a to-one relationship by name.
         *
         * @param name relationship name
         * @return relationship id, or {@code null} if not present
         */
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
