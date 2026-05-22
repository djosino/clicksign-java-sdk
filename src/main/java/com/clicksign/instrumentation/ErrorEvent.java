package com.clicksign.instrumentation;

/** Payload for a request that raised an exception. */
public final class ErrorEvent {

    private final String method;
    private final String path;
    private final int status;
    private final Throwable error;
    private final double durationMs;

    public ErrorEvent(String method, String path, int status, Throwable error, double durationMs) {
        this.method     = method;
        this.path       = path;
        this.status     = status;
        this.error      = error;
        this.durationMs = durationMs;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    /**
     * HTTP status code, or 0 for network/timeout errors.
     *
     * @return HTTP status code
     */
    public int status() {
        return status;
    }

    public Throwable error() {
        return error;
    }

    public double durationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return "ErrorEvent{method='" + method + "', path='" + path + "', status=" + status
            + ", error=" + error.getClass().getSimpleName() + "}";
    }
}
