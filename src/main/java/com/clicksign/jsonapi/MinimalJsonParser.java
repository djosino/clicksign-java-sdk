package com.clicksign.jsonapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursive-descent JSON parser — no external dependencies.
 *
 * <p>Handles the full JSON spec needed for JSON:API responses:
 * objects, arrays, strings (with escapes), numbers, booleans, null.
 */
public final class MinimalJsonParser {

    private final String input;
    private int pos;

    MinimalJsonParser(String input) {
        this.input = input;
        this.pos = 0;
    }

    /**
     * Parses a JSON object string and returns it as a Map.
     *
     * @param json JSON object string
     * @return parsed map
     */
    public static Map<String, Object> parseObject(String json) {
        return new MinimalJsonParser(json).parseObject();
    }

    static JsonApiParser.ParsedResponse parseResponse(String json) {
        MinimalJsonParser p = new MinimalJsonParser(json);
        Map<String, Object> root = p.parseObject();

        List<JsonApiParser.ResourceObject> data = parseData(root.get("data"));
        List<JsonApiParser.ResourceObject> included = parseIncluded(root.get("included"));
        String nextLink = parseNextLink(root.get("links"));

        return new JsonApiParser.ParsedResponse(data, included, nextLink);
    }

    @SuppressWarnings("unchecked")
    private static List<JsonApiParser.ResourceObject> parseData(Object raw) {
        if (raw instanceof List) {
            List<JsonApiParser.ResourceObject> result = new ArrayList<>();
            for (Object item : (List<?>) raw) {
                if (item instanceof Map) {
                    result.add(buildResource((Map<String, Object>) item));
                }
            }
            return result;
        }
        if (raw instanceof Map) {
            return Collections.singletonList(buildResource((Map<String, Object>) raw));
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static List<JsonApiParser.ResourceObject> parseIncluded(Object raw) {
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<JsonApiParser.ResourceObject> result = new ArrayList<>();
        for (Object item : (List<?>) raw) {
            if (item instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) item;
                if (m.containsKey("type")) {
                    result.add(buildResource(m));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String parseNextLink(Object raw) {
        if (!(raw instanceof Map)) {
            return null;
        }
        Object next = ((Map<String, Object>) raw).get("next");
        return next instanceof String && !((String) next).isBlank() ? (String) next : null;
    }

    @SuppressWarnings("unchecked")
    private static JsonApiParser.ResourceObject buildResource(Map<String, Object> map) {
        String id = str(map.get("id"));
        String type = str(map.get("type"));
        Map<String, Object> attrs = map.get("attributes") instanceof Map
            ? (Map<String, Object>) map.get("attributes") : Collections.emptyMap();
        Map<String, Object> rels = map.get("relationships") instanceof Map
            ? (Map<String, Object>) map.get("relationships") : Collections.emptyMap();
        return new JsonApiParser.ResourceObject(id, type, attrs, rels);
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    // ── Parser ──────────────────────────────────────────────────────────────

    Object parseValue() {
        skipWhitespace();
        if (pos >= input.length()) {
            return null;
        }
        char c = input.charAt(pos);
        if (c == '{') {
            return parseObject();
        }
        if (c == '[') {
            return parseArray();
        }
        if (c == '"') {
            return parseString();
        }
        if (c == 't') {
            return parseLiteral("true", Boolean.TRUE);
        }
        if (c == 'f') {
            return parseLiteral("false", Boolean.FALSE);
        }
        if (c == 'n') {
            return parseLiteral("null", null);
        }
        return parseNumber();
    }

    Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object val = parseValue();
            map.put(key, val);
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                break;
            }
            expect(',');
        }
        return map;
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            list.add(parseValue());
            skipWhitespace();
            if (peek() == ']') {
                pos++;
                break;
            }
            expect(',');
        }
        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                char esc = input.charAt(pos++);
                char mapped;
                if (esc == '"') {
                    mapped = '"';
                } else if (esc == '\\') {
                    mapped = '\\';
                } else if (esc == '/') {
                    mapped = '/';
                } else if (esc == 'n') {
                    mapped = '\n';
                } else if (esc == 'r') {
                    mapped = '\r';
                } else if (esc == 't') {
                    mapped = '\t';
                } else if (esc == 'b') {
                    mapped = '\b';
                } else if (esc == 'f') {
                    mapped = '\f';
                } else if (esc == 'u') {
                    if (pos + 4 > input.length()) {
                        throw new IllegalStateException("Incomplete \\uXXXX escape at " + pos);
                    }
                    mapped = (char) Integer.parseInt(input.substring(pos, pos + 4), 16);
                    pos += 4;
                } else {
                    mapped = esc;
                }
                sb.append(mapped);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Object parseLiteral(String literal, Object value) {
        if (input.startsWith(literal, pos)) {
            pos += literal.length();
            return value;
        }
        throw new IllegalStateException("Unexpected token at " + pos);
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        while (pos < input.length() && (Character.isDigit(input.charAt(pos))
               || input.charAt(pos) == '.'
               || input.charAt(pos) == 'e'
               || input.charAt(pos) == 'E'
               || input.charAt(pos) == '+'
               || input.charAt(pos) == '-')) {
            pos++;
        }
        String num = input.substring(start, pos);
        if (num.contains(".") || num.contains("e") || num.contains("E")) {
            return Double.parseDouble(num);
        }
        return Long.parseLong(num);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private void expect(char c) {
        skipWhitespace();
        if (pos >= input.length() || input.charAt(pos) != c) {
            throw new IllegalStateException("Expected '" + c + "' at " + pos);
        }
        pos++;
    }

    private char peek() {
        skipWhitespace();
        return pos < input.length() ? input.charAt(pos) : 0;
    }
}
