package com.clicksign.resources.types;

/** Notification trigger for a signature watcher. */
public enum SignatureWatcherKind implements ApiStringEnum {
    /** Notify at all signing steps. */
    ALL_STEPS("all_steps"),
    /** Notify only when signing is finished. */
    ON_FINISHED("on_finished");

    private final String apiValue;

    SignatureWatcherKind(String apiValue) {
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
