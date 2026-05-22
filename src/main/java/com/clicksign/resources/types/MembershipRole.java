package com.clicksign.resources.types;

/** Account membership role (access level). */
public enum MembershipRole implements ApiStringEnum {
    /** Admin role. */
    ADMIN("admin"),
    /** Member role. */
    MEMBER("member");

    private final String apiValue;

    MembershipRole(String apiValue) {
        this.apiValue = apiValue;
    }

    /**
     * Returns the api value.
     *
     * @return api value
     */
    @Override
    public String apiValue() {
        return apiValue;
    }
}
