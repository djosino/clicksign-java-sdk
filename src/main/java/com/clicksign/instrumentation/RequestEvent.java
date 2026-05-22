package com.clicksign.instrumentation;

/** Payload for a completed HTTP request (success or HTTP error). */
public final class RequestEvent {

    private final String method;
    private final String path;
    private final int status;
    private final int attempt;
    private final double durationMs;

    /**
     * Constructs a request event.
     *
     * @param method     HTTP method
     * @param path       request path
     * @param status     HTTP status code
     * @param attempt    attempt number (1-based)
     * @param durationMs request duration in milliseconds
     */
    public RequestEvent(String method, String path, int status, int attempt, double durationMs) {
        this.method     = method;
        this.path       = path;
        this.status     = status;
        this.attempt    = attempt;
        this.durationMs = durationMs;
    }

    /**
     * Returns the HTTP method.
     *
     * @return method
     */
    public String method() {
        return method;
    }

    /**
     * Returns the request path.
     *
     * @return path
     */
    public String path() {
        return path;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return status code
     */
    public int status() {
        return status;
    }

    /**
     * Returns the attempt number.
     *
     * @return attempt number
     */
    public int attempt() {
        return attempt;
    }

    /**
     * Returns the request duration in milliseconds.
     *
     * @return duration in milliseconds
     */
    public double durationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return "RequestEvent{method='" + method + "', path='" + path + "', status=" + status
            + ", attempt=" + attempt + ", durationMs=" + durationMs + "}";
    }
}
