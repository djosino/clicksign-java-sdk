package com.clicksign.resources.types;

/** Role for a signing requirement participant. */
public enum RequirementRole implements ApiStringEnum {
    /** Sign role. */
    SIGN("sign"),
    /** Approve role. */
    APPROVE("approve"),
    /** Witness role. */
    WITNESS("witness"),
    /** Guarantor role. */
    GUARANTOR("guarantor"),
    /** Contractee role. */
    CONTRACTEE("contractee"),
    /** Contractor role. */
    CONTRACTOR("contractor"),
    /** Administrator role. */
    ADMINISTRATOR("administrator"),
    /** Receipt role. */
    RECEIPT("receipt"),
    /** Endorser role. */
    ENDORSER("endorser"),
    /** Joint debtor role. */
    JOINT_DEBTOR("joint_debtor"),
    /** Attorney role. */
    ATTORNEY("attorney"),
    /** Legal representative role. */
    LEGAL_REPRESENTATIVE("legal_representative");

    private final String apiValue;

    RequirementRole(String apiValue) {
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
