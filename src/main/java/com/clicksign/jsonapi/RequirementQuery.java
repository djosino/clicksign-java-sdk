package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Requirement;
import com.clicksign.resources.types.RequirementAction;
import com.clicksign.resources.types.RequirementRole;

/** Typed filters for {@code GET /envelopes/{id}/requirements}. */
public final class RequirementQuery extends TypedResourceQuery<Requirement, RequirementQuery> {

    /**
     * Constructs this query for the given envelope.
     *
     * @param envelopeId envelope id
     * @param http       HTTP client
     */
    public RequirementQuery(String envelopeId, com.clicksign.http.HttpClient http) {
        super("/envelopes/" + envelopeId + "/requirements", http,
            obj -> Requirement.from(obj, envelopeId));
    }

    /**
     * Filters by requirement action.
     *
     * @param action requirement action
     * @return this query
     */
    public RequirementQuery action(RequirementAction action) {
        return filter("action", action);
    }

    /**
     * Filters by requirement role.
     *
     * @param role requirement role
     * @return this query
     */
    public RequirementQuery role(RequirementRole role) {
        return filter("role", role);
    }
}
