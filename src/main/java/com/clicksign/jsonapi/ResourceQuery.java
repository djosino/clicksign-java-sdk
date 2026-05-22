package com.clicksign.jsonapi;

import com.clicksign.resources.types.ApiStringEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Fluent query builder for JSON:API resource collections.
 *
 * <p>All methods return {@code this} for chaining. Call {@link #fetch()} for one page
 * or {@link #fetchAll()} to follow {@code links.next}.
 *
 * <pre>{@code
 * client.envelopes().filter()
 *     .status(EnvelopeStatus.RUNNING)
 *     .name("Contrato")
 *     .order("-created")
 *     .page(1).perPage(20)
 *     .fetch();
 * }</pre>
 */
public class ResourceQuery<T> {

    private final String endpoint;
    private final com.clicksign.http.HttpClient http;
    private final Function<JsonApiParser.ResourceObject, T> mapper;

    private final Map<String, String> params = new LinkedHashMap<>();
    private String includeParam;

    public ResourceQuery(String endpoint,
                         com.clicksign.http.HttpClient http,
                         Function<JsonApiParser.ResourceObject, T> mapper) {
        this.endpoint = endpoint;
        this.http     = http;
        this.mapper   = mapper;
    }

    public ResourceQuery<T> filter(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("filter key is required");
        }
        if (value == null) {
            throw new IllegalArgumentException("filter value is required");
        }
        params.put("filter[" + key + "]", value);
        return this;
    }

    /** Filters using an enum's {@link ApiStringEnum#apiValue()}. */
    public ResourceQuery<T> filter(String key, ApiStringEnum value) {
        if (value == null) {
            throw new IllegalArgumentException("filter value is required");
        }
        return filter(key, value.apiValue());
    }

    public ResourceQuery<T> order(String field) {
        params.put("sort", field);
        return this;
    }

    public ResourceQuery<T> page(int number) {
        params.put("page[number]", String.valueOf(number));
        return this;
    }

    public ResourceQuery<T> perPage(int size) {
        params.put("page[size]", String.valueOf(size));
        return this;
    }

    public ResourceQuery<T> include(String... types) {
        String existing = includeParam;
        if (existing == null || existing.isBlank()) {
            includeParam = String.join(",", types);
        } else {
            Set<String> all = new LinkedHashSet<>(Arrays.asList(existing.split(",")));
            Collections.addAll(all, types);
            includeParam = String.join(",", all);
        }
        return this;
    }

    public ResourceQuery<T> fields(String type, String... fieldNames) {
        params.put("fields[" + type + "]", String.join(",", fieldNames));
        return this;
    }

    /** Fetches a single page with current params. */
    public List<T> fetch() {
        Map<String, String> queryParams = buildQueryParams();
        String raw = http.get(endpoint, queryParams);
        List<T> result = new ArrayList<>();
        for (JsonApiParser.ResourceObject obj : JsonApiParser.parse(raw).data()) {
            result.add(mapper.apply(obj));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Auto-paginates through all pages using {@code links.next}.
     * Falls back to item-count heuristic when API omits links.
     */
    public List<T> fetchAll() {
        int pageSize;
        try {
            pageSize = Integer.parseInt(params.getOrDefault("page[size]", "20"));
        } catch (NumberFormatException e) {
            pageSize = 20;
        }
        Map<String, String> base = buildQueryParams();
        base.remove("page[number]");

        List<T> all = new ArrayList<>();
        int page = 1;

        while (true) {
            Map<String, String> queryParams = new LinkedHashMap<>(base);
            queryParams.put("page[number]", String.valueOf(page));
            queryParams.put("page[size]",   String.valueOf(pageSize));

            String raw = http.get(endpoint, queryParams);
            JsonApiParser.ParsedResponse parsed = JsonApiParser.parse(raw);

            List<T> items = new ArrayList<>();
            for (JsonApiParser.ResourceObject obj : parsed.data()) {
                items.add(mapper.apply(obj));
            }
            all.addAll(items);

            if (parsed.nextLink() != null) {
                page++;
            } else if (!items.isEmpty() && items.size() >= pageSize) {
                // links key absent — heuristic: if full page returned, there may be more
                page++;
            } else {
                break;
            }
        }
        return Collections.unmodifiableList(all);
    }

    private Map<String, String> buildQueryParams() {
        Map<String, String> p = new LinkedHashMap<>(params);
        if (includeParam != null) {
            p.put("include", includeParam);
        }
        return p;
    }
}
