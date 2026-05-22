package com.clicksign.jsonapi;

import com.clicksign.resources.Membership;
import com.clicksign.resources.types.MembershipRole;

/** Typed filters for {@code GET /memberships}. */
public final class MembershipQuery extends TypedResourceQuery<Membership, MembershipQuery> {

    public MembershipQuery(com.clicksign.http.HttpClient http) {
        super("/memberships", http, Membership::from);
    }

    public MembershipQuery role(MembershipRole role) {
        return filter("role", role);
    }

    /** Filter by related user id ({@code filter[user.id]}). */
    public MembershipQuery userId(String userId) {
        return filter("user.id", userId);
    }
}
