package com.clicksign.instrumentation;

/** Payload for a request that raised an exception. */
public final class ErrorEvent {

    private final String method;
    private final String path;
    private final int status;
    private final Throwable error;
    private final double durationMs;

    /**
     * Constructs an error event.
     *
     * @param method     HTTP method
     * @param path       request path
     * @param status     HTTP status code, or 0 for network/timeout errors
     * @param error      the exception that was raised
     * @param durationMs request duration in milliseconds
     */
    public ErrorEvent(String method, String path, int status, Throwable error, double durationMs) {
        this.method     = method;
        this.path       = path;
        this.status     = status;
        this.error      = error;
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
     * HTTP status code, or 0 for network/timeout errors.
     *
     * @return HTTP status code
     */
    public int status() {
        return status;
    }

    /**
     * Returns the exception that was raised.
     *
     * @return error
     */
    public Throwable error() {
        return error;
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
        return "ErrorEvent{method='" + method + "', path='" + path + "', status=" + status
            + ", error=" + error.getClass().getSimpleName() + "}";
    }
}
