package com.clicksign.resources.types;

public enum SenderNameOption implements ApiStringEnum {
    USER_NAME("user_name"),
    ACCOUNT_NAME("account_name"),
    USER_AND_ACCOUNT_NAME("user_and_account_name");

    private final String apiValue;

    SenderNameOption(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
