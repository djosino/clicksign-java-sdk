package com.clicksign.jsonapi;

import com.clicksign.resources.types.ApiStringEnum;

import java.util.function.Function;

/**
 * {@link ResourceQuery} with covariant return type for fluent chaining of filters, sort and pagination.
 *
 * @param <T> resource type returned by this query
 * @param <Q> concrete subclass type for covariant return
 */
public abstract class TypedResourceQuery<T, Q extends TypedResourceQuery<T, Q>> extends ResourceQuery<T> {

    /**
     * Constructs a typed query.
     *
     * @param endpoint API endpoint path
     * @param http     HTTP client
     * @param mapper   function mapping resource objects to typed instances
     */
    protected TypedResourceQuery(String endpoint,
                                 com.clicksign.http.HttpClient http,
                                 Function<JsonApiParser.ResourceObject, T> mapper) {
        super(endpoint, http, mapper);
    }

    /**
     * Returns this instance cast to the concrete subtype.
     *
     * @return this query as {@code Q}
     */
    @SuppressWarnings("unchecked")
    protected final Q self() {
        return (Q) this;
    }

    /** {@inheritDoc} */
    @Override
    public Q filter(String key, String value) {
        super.filter(key, value);
        return self();
    }

    /**
     * Filters using an enum's api value.
     *
     * @param key   filter key
     * @param value enum value
     * @return this query
     */
    public Q filter(String key, ApiStringEnum value) {
        super.filter(key, value);
        return self();
    }

    /** {@inheritDoc} */
    @Override
    public Q order(String field) {
        super.order(field);
        return self();
    }

    /** {@inheritDoc} */
    @Override
    public Q page(int number) {
        super.page(number);
        return self();
    }

    /** {@inheritDoc} */
    @Override
    public Q perPage(int size) {
        super.perPage(size);
        return self();
    }

    /** {@inheritDoc} */
    @Override
    public Q include(String... types) {
        super.include(types);
        return self();
    }

    /** {@inheritDoc} */
    @Override
    public Q fields(String type, String... fieldNames) {
        super.fields(type, fieldNames);
        return self();
    }
}
