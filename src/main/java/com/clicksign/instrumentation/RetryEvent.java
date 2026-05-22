package com.clicksign.instrumentation;

/** Payload for a retry that is about to be attempted. */
public final class RetryEvent {

    private final String method;
    private final String path;
    private final int attempt;
    private final int maxRetries;
    private final Throwable error;
    private final long waitMs;

    public RetryEvent(String method, String path, int attempt, int maxRetries,
                      Throwable error, long waitMs) {
        this.method     = method;
        this.path       = path;
        this.attempt    = attempt;
        this.maxRetries = maxRetries;
        this.error      = error;
        this.waitMs     = waitMs;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public int attempt() {
        return attempt;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public Throwable error() {
        return error;
    }

    public long waitMs() {
        return waitMs;
    }

    @Override
    public String toString() {
        return "RetryEvent{method='" + method + "', path='" + path + "', attempt=" + attempt
            + ", waitMs=" + waitMs + "}";
    }
}
