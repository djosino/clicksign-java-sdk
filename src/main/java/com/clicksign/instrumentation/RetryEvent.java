package com.clicksign.instrumentation;

/** Payload for a retry that is about to be attempted. */
public final class RetryEvent {

    private final String method;
    private final String path;
    private final int attempt;
    private final int maxRetries;
    private final Throwable error;
    private final long waitMs;

    /**
     * Constructs a retry event.
     *
     * @param method     HTTP method
     * @param path       request path
     * @param attempt    current attempt number (1-based)
     * @param maxRetries maximum number of retries configured
     * @param error      the exception that triggered the retry
     * @param waitMs     milliseconds to wait before next attempt
     */
    public RetryEvent(String method, String path, int attempt, int maxRetries,
                      Throwable error, long waitMs) {
        this.method     = method;
        this.path       = path;
        this.attempt    = attempt;
        this.maxRetries = maxRetries;
        this.error      = error;
        this.waitMs     = waitMs;
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
     * Returns the current attempt number.
     *
     * @return attempt number
     */
    public int attempt() {
        return attempt;
    }

    /**
     * Returns the maximum number of retries configured.
     *
     * @return max retries
     */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Returns the exception that triggered the retry.
     *
     * @return error
     */
    public Throwable error() {
        return error;
    }

    /**
     * Returns the milliseconds to wait before next attempt.
     *
     * @return wait in milliseconds
     */
    public long waitMs() {
        return waitMs;
    }

    @Override
    public String toString() {
        return "RetryEvent{method='" + method + "', path='" + path + "', attempt=" + attempt
            + ", waitMs=" + waitMs + "}";
    }
}
