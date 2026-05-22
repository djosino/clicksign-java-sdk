package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.types.EnvelopeStatus;

/** Typed filters for {@code GET /envelopes}. */
public final class EnvelopeQuery extends TypedResourceQuery<Envelope, EnvelopeQuery> {

    public EnvelopeQuery(com.clicksign.http.HttpClient http) {
        super("/envelopes", http, Envelope::from);
    }

    public EnvelopeQuery status(EnvelopeStatus status) {
        return filter("status", status);
    }

    public EnvelopeQuery name(String name) {
        return filter("name", name);
    }

    /** JSON:API date filter, e.g. {@code 2026-01-01,2026-12-31}. */
    public EnvelopeQuery created(String range) {
        return filter("created", range);
    }

    /** JSON:API date filter, e.g. {@code 2026-01-01,2026-12-31}. */
    public EnvelopeQuery modified(String range) {
        return filter("modified", range);
    }

    /** JSON:API date filter for {@code deadline_at}. */
    public EnvelopeQuery deadlineAt(String range) {
        return filter("deadline_at", range);
    }

    public EnvelopeQuery orderByName(boolean descending) {
        return order(descending ? "-name" : "name");
    }
}
