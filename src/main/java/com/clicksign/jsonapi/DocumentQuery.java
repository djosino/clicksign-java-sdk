package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.types.DocumentStatus;

/** Typed filters for {@code GET /envelopes/{id}/documents}. */
public final class DocumentQuery extends TypedResourceQuery<Document, DocumentQuery> {

    public DocumentQuery(String envelopeId, com.clicksign.http.HttpClient http) {
        super("/envelopes/" + envelopeId + "/documents", http,
            obj -> Document.from(obj, envelopeId));
    }

    public DocumentQuery status(DocumentStatus status) {
        return filter("status", status);
    }

    public DocumentQuery filename(String filename) {
        return filter("filename", filename);
    }
}
