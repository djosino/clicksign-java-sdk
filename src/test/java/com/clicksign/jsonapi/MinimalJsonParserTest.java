package com.clicksign.jsonapi;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimalJsonParserTest {

    @Test
    void parsesStringValues() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"key\":\"value\"}");
        assertEquals("value", result.get("key"));
    }

    @Test
    void parsesNumberValues() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"n\":42,\"f\":3.14}");
        assertEquals(42L, result.get("n"));
        assertEquals(3.14, (Double) result.get("f"), 0.001);
    }

    @Test
    void parsesBooleanValues() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"t\":true,\"f\":false}");
        assertEquals(Boolean.TRUE,  result.get("t"));
        assertEquals(Boolean.FALSE, result.get("f"));
    }

    @Test
    void parsesNullValue() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"n\":null}");
        assertNull(result.get("n"));
    }

    @Test
    void parsesNestedObject() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"a\":{\"b\":\"c\"}}");
        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) result.get("a");
        assertEquals("c", inner.get("b"));
    }

    @Test
    void parsesArray() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"arr\":[1,2,3]}");
        @SuppressWarnings("unchecked")
        List<Object> arr = (List<Object>) result.get("arr");
        assertEquals(3, arr.size());
        assertEquals(1L, arr.get(0));
    }

    @Test
    void parsesUnicodeEscapes() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"v\":\"\\u00e9\"}");
        assertEquals("é", result.get("v"));
    }

    @Test
    void parsesStringWithEscapedQuotes() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"v\":\"say \\\"hi\\\"\"}");
        assertEquals("say \"hi\"", result.get("v"));
    }

    @Test
    void parsesSingleResourceResponse() {
        String json = "{\"data\":{\"id\":\"1\",\"type\":\"envelopes\","
            + "\"attributes\":{\"name\":\"Test\",\"status\":\"draft\"},\"relationships\":{}}}";
        JsonApiParser.ParsedResponse parsed = JsonApiParser.parse(json);
        assertEquals(1, parsed.data().size());
        assertEquals("1", parsed.data().get(0).id());
        assertEquals("Test", parsed.data().get(0).attributes().get("name"));
    }

    @Test
    void parsesCollectionResponse() {
        String json = "{\"data\":["
            + "{\"id\":\"1\",\"type\":\"envelopes\",\"attributes\":{\"name\":\"A\"},\"relationships\":{}},"
            + "{\"id\":\"2\",\"type\":\"envelopes\",\"attributes\":{\"name\":\"B\"},\"relationships\":{}}"
            + "]}";
        JsonApiParser.ParsedResponse parsed = JsonApiParser.parse(json);
        assertEquals(2, parsed.data().size());
        assertEquals("A", parsed.data().get(0).attributes().get("name"));
        assertEquals("B", parsed.data().get(1).attributes().get("name"));
    }

    @Test
    void parsesLinksNext() {
        String json = "{\"data\":[],\"links\":{\"next\":\"https://api.example.com?page=2\"}}";
        JsonApiParser.ParsedResponse parsed = JsonApiParser.parse(json);
        assertEquals("https://api.example.com?page=2", parsed.nextLink());
    }

    @Test
    void returnsNullNextLinkWhenAbsent() {
        String json = "{\"data\":[]}";
        assertNull(JsonApiParser.parse(json).nextLink());
    }

    @Test
    void returnsNullNextLinkWhenLinksHasNoNext() {
        String json = "{\"data\":[],\"links\":{}}";
        assertNull(JsonApiParser.parse(json).nextLink());
    }

    @Test
    void returnsNullNextLinkWhenNextIsNull() {
        String json = "{\"data\":[],\"links\":{\"next\":null}}";
        assertNull(JsonApiParser.parse(json).nextLink());
    }

    @Test
    void parsesRelationshipId() {
        String json = "{\"data\":{\"id\":\"r1\",\"type\":\"requirements\","
            + "\"attributes\":{},"
            + "\"relationships\":{\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"env1\"}}}}}";
        JsonApiParser.ParsedResponse parsed = JsonApiParser.parse(json);
        assertEquals("env1", parsed.data().get(0).relationshipId("envelope"));
    }

    @Test
    void parsesEmptyResponse() {
        JsonApiParser.ParsedResponse parsed = JsonApiParser.parse(null);
        assertTrue(parsed.data().isEmpty());
    }

    @Test
    void parsesAllCommonEscapeSequences() {
        Map<String, Object> result = MinimalJsonParser.parseObject(
            "{\"v\":\"\\n\\r\\t\\b\\f\\\\\\/\"}");
        assertEquals("\n\r\t\b\f\\/", result.get("v"));
    }

    @Test
    void throwsOnNonHexUnicodeEscape() {
        // "12}" is read as the 4-char substring — parseInt fails on non-hex char
        assertThrows(NumberFormatException.class, () ->
            MinimalJsonParser.parseObject("{\"v\":\"\\u12\"}"));
    }

    @Test
    void throwsOnTruncatedUnicodeEscape() {
        // input ends before 4 hex digits: bounds check fires with IllegalStateException
        assertThrows(IllegalStateException.class, () ->
            MinimalJsonParser.parseObject("{\"v\":\"\\u"));
    }

    @Test
    void throwsOnTruncatedUnicodeEscapeWith3Digits() {
        // 3 valid hex digits then end of input: bounds check fires with IllegalStateException
        assertThrows(IllegalStateException.class, () ->
            MinimalJsonParser.parseObject("{\"v\":\"\\u00A"));
    }

    @Test
    void parsesUnicodeBmp() {
        Map<String, Object> result = MinimalJsonParser.parseObject("{\"v\":\"\\u0041\"}");
        assertEquals("A", result.get("v"));
    }

    @Test
    void throwsOnMalformedJson() {
        assertThrows(Exception.class, () ->
            MinimalJsonParser.parseObject("{\"key\":}"));
    }

    @Test
    void throwsOnTruncatedBackslash() {
        assertThrows(IllegalStateException.class, () ->
            MinimalJsonParser.parseObject("{\"v\":\"abc\\"));
    }
}
