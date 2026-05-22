package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Signer;

/** Typed filters for {@code GET /envelopes/{id}/signers}. */
public final class SignerQuery extends TypedResourceQuery<Signer, SignerQuery> {

    /**
     * Constructs this query for the given envelope.
     *
     * @param envelopeId envelope id
     * @param http       HTTP client
     */
    public SignerQuery(String envelopeId, com.clicksign.http.HttpClient http) {
        super("/envelopes/" + envelopeId + "/signers", http,
            obj -> Signer.from(obj, envelopeId));
    }

    /**
     * Filters by signer name.
     *
     * @param name signer name
     * @return this query
     */
    public SignerQuery name(String name) {
        return filter("name", name);
    }

    /**
     * Filters by signer email.
     *
     * @param email signer email
     * @return this query
     */
    public SignerQuery email(String email) {
        return filter("email", email);
    }
}
