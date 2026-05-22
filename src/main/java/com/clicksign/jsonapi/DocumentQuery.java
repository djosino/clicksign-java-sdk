package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.types.DocumentStatus;

/** Typed filters for {@code GET /envelopes/{id}/documents}. */
public final class DocumentQuery extends TypedResourceQuery<Document, DocumentQuery> {

    /**
     * Constructs this query for the given envelope.
     *
     * @param envelopeId envelope id
     * @param http HTTP client
     */
    public DocumentQuery(String envelopeId, com.clicksign.http.HttpClient http) {
        super("/envelopes/" + envelopeId + "/documents", http,
            obj -> Document.from(obj, envelopeId));
    }

    /**
     * Filters by document status.
     *
     * @param status document status
     * @return this query
     */
    public DocumentQuery status(DocumentStatus status) {
        return filter("status", status);
    }

    /**
     * Filters by filename.
     *
     * @param filename document filename
     * @return this query
     */
    public DocumentQuery filename(String filename) {
        return filter("filename", filename);
    }
}
