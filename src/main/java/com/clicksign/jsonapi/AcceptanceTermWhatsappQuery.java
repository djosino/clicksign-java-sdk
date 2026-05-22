package com.clicksign.jsonapi;

import com.clicksign.resources.AcceptanceTermWhatsapp;
import com.clicksign.resources.types.AcceptanceTermStatus;

/** Typed filters for {@code GET /acceptance_term/whatsapps}. */
public final class AcceptanceTermWhatsappQuery
        extends TypedResourceQuery<AcceptanceTermWhatsapp, AcceptanceTermWhatsappQuery> {

    /**
     * Constructs a query for the acceptance term WhatsApp endpoint.
     *
     * @param http HTTP client
     */
    public AcceptanceTermWhatsappQuery(com.clicksign.http.HttpClient http) {
        super("/acceptance_term/whatsapps", http, AcceptanceTermWhatsapp::from);
    }

    /**
     * Filters results by acceptance term status.
     *
     * @param status status filter value
     * @return this query builder
     */
    public AcceptanceTermWhatsappQuery status(AcceptanceTermStatus status) {
        return filter("status", status);
    }
}
