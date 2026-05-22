package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Signer;

/** Typed filters for {@code GET /envelopes/{id}/signers}. */
public final class SignerQuery extends TypedResourceQuery<Signer, SignerQuery> {

    public SignerQuery(String envelopeId, com.clicksign.http.HttpClient http) {
        super("/envelopes/" + envelopeId + "/signers", http,
            obj -> Signer.from(obj, envelopeId));
    }

    public SignerQuery name(String name) {
        return filter("name", name);
    }

    public SignerQuery email(String email) {
        return filter("email", email);
    }
}
