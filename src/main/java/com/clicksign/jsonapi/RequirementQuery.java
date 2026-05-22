package com.clicksign.jsonapi;

import com.clicksign.resources.notarial.Requirement;
import com.clicksign.resources.types.RequirementAction;
import com.clicksign.resources.types.RequirementRole;

/** Typed filters for {@code GET /envelopes/{id}/requirements}. */
public final class RequirementQuery extends TypedResourceQuery<Requirement, RequirementQuery> {

    public RequirementQuery(String envelopeId, com.clicksign.http.HttpClient http) {
        super("/envelopes/" + envelopeId + "/requirements", http,
            obj -> Requirement.from(obj, envelopeId));
    }

    public RequirementQuery action(RequirementAction action) {
        return filter("action", action);
    }

    public RequirementQuery role(RequirementRole role) {
        return filter("role", role);
    }
}
