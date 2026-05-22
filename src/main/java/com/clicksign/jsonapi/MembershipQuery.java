package com.clicksign.jsonapi;

import com.clicksign.resources.Membership;
import com.clicksign.resources.types.MembershipRole;

/** Typed filters for {@code GET /memberships}. */
public final class MembershipQuery extends TypedResourceQuery<Membership, MembershipQuery> {

    /**
     * Constructs this query.
     *
     * @param http HTTP client
     */
    public MembershipQuery(com.clicksign.http.HttpClient http) {
        super("/memberships", http, Membership::from);
    }

    /**
     * Filters by membership role.
     *
     * @param role membership role
     * @return this query
     */
    public MembershipQuery role(MembershipRole role) {
        return filter("role", role);
    }

    /**
     * Filter by related user id ({@code filter[user.id]}).
     *
     * @param userId user id to filter by
     * @return this query
     */
    public MembershipQuery userId(String userId) {
        return filter("user.id", userId);
    }
}
