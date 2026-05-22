package com.clicksign.jsonapi;

import com.clicksign.resources.types.ApiStringEnum;

import java.util.function.Function;

/**
 * {@link ResourceQuery} with covariant return type for fluent chaining of filters, sort and pagination.
 */
public abstract class TypedResourceQuery<T, Q extends TypedResourceQuery<T, Q>> extends ResourceQuery<T> {

    protected TypedResourceQuery(String endpoint,
                                 com.clicksign.http.HttpClient http,
                                 Function<JsonApiParser.ResourceObject, T> mapper) {
        super(endpoint, http, mapper);
    }

    @SuppressWarnings("unchecked")
    protected final Q self() {
        return (Q) this;
    }

    @Override
    public Q filter(String key, String value) {
        super.filter(key, value);
        return self();
    }

    public Q filter(String key, ApiStringEnum value) {
        super.filter(key, value);
        return self();
    }

    @Override
    public Q order(String field) {
        super.order(field);
        return self();
    }

    @Override
    public Q page(int number) {
        super.page(number);
        return self();
    }

    @Override
    public Q perPage(int size) {
        super.perPage(size);
        return self();
    }

    @Override
    public Q include(String... types) {
        super.include(types);
        return self();
    }

    @Override
    public Q fields(String type, String... fieldNames) {
        super.fields(type, fieldNames);
        return self();
    }
}
