package com.clicksign.instrumentation;

/** Payload for a completed HTTP request (success or HTTP error). */
public final class RequestEvent {

    private final String method;
    private final String path;
    private final int status;
    private final int attempt;
    private final double durationMs;

    public RequestEvent(String method, String path, int status, int attempt, double durationMs) {
        this.method     = method;
        this.path       = path;
        this.status     = status;
        this.attempt    = attempt;
        this.durationMs = durationMs;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public int status() {
        return status;
    }

    public int attempt() {
        return attempt;
    }

    public double durationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return "RequestEvent{method='" + method + "', path='" + path + "', status=" + status
            + ", attempt=" + attempt + ", durationMs=" + durationMs + "}";
    }
}
