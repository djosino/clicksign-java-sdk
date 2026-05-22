package com.clicksign.instrumentation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * SDK-wide instrumentation registry.
 *
 * <p>Register callbacks via {@link com.clicksign.ClicksignClient.Builder#onRequest},
 * {@link com.clicksign.ClicksignClient.Builder#onRetry},
 * {@link com.clicksign.ClicksignClient.Builder#onError}. Callbacks are invoked synchronously on the request thread.
 * Exceptions thrown by callbacks are caught and logged — they do not propagate.
 *
 * <pre>{@code
 * ClicksignClient client = ClicksignClient.builder()
 *     .apiKey(...)
 *     .onRequest(e -> log.info("{} {} {} {}ms", e.method(), e.path(), e.status(), e.durationMs()))
 *     .onError(e   -> metrics.increment("clicksign.errors", "status:" + e.status()))
 *     .build();
 * }</pre>
 */
public final class Instrumentation {

    private static final Logger LOG = Logger.getLogger(Instrumentation.class.getName());

    private final List<Consumer<RequestEvent>> requestListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<RetryEvent>>   retryListeners   = new CopyOnWriteArrayList<>();
    private final List<Consumer<ErrorEvent>>   errorListeners   = new CopyOnWriteArrayList<>();

    public void onRequest(Consumer<RequestEvent> listener) { requestListeners.add(listener); }
    public void onRetry(Consumer<RetryEvent> listener)     { retryListeners.add(listener); }
    public void onError(Consumer<ErrorEvent> listener)     { errorListeners.add(listener); }

    public void publishRequest(RequestEvent event) {
        for (Consumer<RequestEvent> l : requestListeners) safeInvoke(l, event);
    }

    public void publishRetry(RetryEvent event) {
        for (Consumer<RetryEvent> l : retryListeners) safeInvoke(l, event);
    }

    public void publishError(ErrorEvent event) {
        for (Consumer<ErrorEvent> l : errorListeners) safeInvoke(l, event);
    }

    private <E> void safeInvoke(Consumer<E> listener, E event) {
        try {
            listener.accept(event);
        } catch (Exception e) {
            LOG.warning("[Clicksign] instrumentation callback error: " + e.getMessage());
        }
    }
}
