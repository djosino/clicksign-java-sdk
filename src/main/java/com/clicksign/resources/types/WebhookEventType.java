package com.clicksign.resources.types;

/** Event type for a webhook subscription. */
public enum WebhookEventType implements ApiStringEnum {
    /** Sign event. */
    SIGN("sign"),
    /** Close event. */
    CLOSE("close"),
    /** Refusal event. */
    REFUSAL("refusal"),
    /** Auto close event. */
    AUTO_CLOSE("auto_close"),
    /** Document closed event. */
    DOCUMENT_CLOSED("document_closed"),
    /** Cancel event. */
    CANCEL("cancel"),
    /** Deadline event. */
    DEADLINE("deadline");

    private final String apiValue;

    WebhookEventType(String apiValue) {
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
