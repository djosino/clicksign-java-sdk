package com.clicksign.resources.types;

/** Account membership role (access level). */
public enum MembershipRole implements ApiStringEnum {
    ADMIN("admin"),
    MEMBER("member");

    private final String apiValue;

    MembershipRole(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
