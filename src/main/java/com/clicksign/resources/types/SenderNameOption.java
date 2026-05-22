package com.clicksign.resources.types;

/** Sender name display option for email notifications. */
public enum SenderNameOption implements ApiStringEnum {
    /** Display the user name. */
    USER_NAME("user_name"),
    /** Display the account name. */
    ACCOUNT_NAME("account_name"),
    /** Display both user and account name. */
    USER_AND_ACCOUNT_NAME("user_and_account_name");

    private final String apiValue;

    SenderNameOption(String apiValue) {
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
