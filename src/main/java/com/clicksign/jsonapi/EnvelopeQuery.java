package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.EnvelopeStatus;

/** Typed filters for {@code GET /envelopes}. */
public final class EnvelopeQuery extends TypedResourceQuery<Envelope, EnvelopeQuery> {

    /**
     * Constructs this query.
     *
     * @param http HTTP client
     */
    public EnvelopeQuery(com.clicksign.http.HttpClient http) {
        super("/envelopes", http, Envelope::from);
    }

    /**
     * Filters by envelope status.
     *
     * @param status envelope status
     * @return this query
     */
    public EnvelopeQuery status(EnvelopeStatus status) {
        return filter("status", status);
    }

    /**
     * Filters by envelope name.
     *
     * @param name envelope name
     * @return this query
     */
    public EnvelopeQuery name(String name) {
        return filter("name", name);
    }

    /**
     * JSON:API date filter, e.g. {@code 2026-01-01,2026-12-31}.
     *
     * @param range date range string
     * @return this query
     */
    public EnvelopeQuery created(String range) {
        return filter("created", range);
    }

    /**
     * JSON:API date filter, e.g. {@code 2026-01-01,2026-12-31}.
     *
     * @param range date range string
     * @return this query
     */
    public EnvelopeQuery modified(String range) {
        return filter("modified", range);
    }

    /**
     * JSON:API date filter for {@code deadline_at}.
     *
     * @param range date range string
     * @return this query
     */
    public EnvelopeQuery deadlineAt(String range) {
        return filter("deadline_at", range);
    }

    /**
     * Orders results by name.
     *
     * @param descending true for descending order
     * @return this query
     */
    public EnvelopeQuery orderByName(boolean descending) {
        return order(descending ? "-name" : "name");
    }
}
