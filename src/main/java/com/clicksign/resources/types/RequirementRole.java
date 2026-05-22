package com.clicksign.resources.types;

public enum RequirementRole implements ApiStringEnum {
    SIGN("sign"),
    APPROVE("approve"),
    WITNESS("witness"),
    GUARANTOR("guarantor"),
    CONTRACTEE("contractee"),
    CONTRACTOR("contractor"),
    ADMINISTRATOR("administrator"),
    RECEIPT("receipt"),
    ENDORSER("endorser"),
    JOINT_DEBTOR("joint_debtor"),
    ATTORNEY("attorney"),
    LEGAL_REPRESENTATIVE("legal_representative");

    private final String apiValue;

    RequirementRole(String apiValue) { this.apiValue = apiValue; }

    @Override
    public String apiValue() { return apiValue; }
}
