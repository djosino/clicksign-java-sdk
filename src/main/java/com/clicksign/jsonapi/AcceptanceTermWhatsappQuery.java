package com.clicksign.jsonapi;

import com.clicksign.resources.AcceptanceTermWhatsapp;
import com.clicksign.resources.types.AcceptanceTermStatus;

/** Typed filters for {@code GET /acceptance_term/whatsapps}. */
public final class AcceptanceTermWhatsappQuery
        extends TypedResourceQuery<AcceptanceTermWhatsapp, AcceptanceTermWhatsappQuery> {

    public AcceptanceTermWhatsappQuery(com.clicksign.http.HttpClient http) {
        super("/acceptance_term/whatsapps", http, AcceptanceTermWhatsapp::from);
    }

    public AcceptanceTermWhatsappQuery status(AcceptanceTermStatus status) {
        return filter("status", status);
    }
}
