package com.clicksign.resources.types;

public enum WebhookEventType implements ApiStringEnum {
    SIGN("sign"),
    CLOSE("close"),
    REFUSAL("refusal"),
    AUTO_CLOSE("auto_close"),
    DOCUMENT_CLOSED("document_closed"),
    CANCEL("cancel"),
    DEADLINE("deadline");

    private final String apiValue;

    WebhookEventType(String apiValue) { this.apiValue = apiValue; }

    @Override
    public String apiValue() { return apiValue; }
}
