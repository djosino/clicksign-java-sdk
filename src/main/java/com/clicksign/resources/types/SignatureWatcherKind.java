package com.clicksign.resources.types;

public enum SignatureWatcherKind implements ApiStringEnum {
    ALL_STEPS("all_steps"),
    ON_FINISHED("on_finished");

    private final String apiValue;

    SignatureWatcherKind(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String apiValue() {
        return apiValue;
    }
}
