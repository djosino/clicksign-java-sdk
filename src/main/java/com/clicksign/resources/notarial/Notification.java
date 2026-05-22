package com.clicksign.resources.notarial;

import com.clicksign.jsonapi.JsonApiParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Result of notifying signers on an envelope. */
public final class Notification {

    private final String id;
    private final String message;
    private final List<SummaryEntry> summary;
    private final String createdAt;

    @SuppressWarnings("unchecked")
    private Notification(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id        = obj.id();
        this.message   = str(a.get("message"));
        this.createdAt = str(a.get("created"));

        List<SummaryEntry> entries = new ArrayList<>();
        Object rawSummary = a.get("summary");
        if (rawSummary instanceof List) {
            for (Object item : (List<?>) rawSummary) {
                if (item instanceof Map) {
                    Map<String, Object> m = (Map<String, Object>) item;
                    entries.add(new SummaryEntry(
                        str(m.get("signer_id")),
                        bool(m.get("notified"))));
                }
            }
        }
        this.summary = Collections.unmodifiableList(entries);
    }

    static Notification fromResource(JsonApiParser.ResourceObject obj) {
        return new Notification(obj);
    }

    /**
     * Returns the id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Returns the message.
     *
     * @return message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the notification summary entries.
     *
     * @return summary
     */
    public List<SummaryEntry> summary() {
        return summary;
    }

    /**
     * Returns the created at timestamp.
     *
     * @return created at
     */
    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Notification{id='" + id + "', summarySize=" + summary.size() + "}";
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private static boolean bool(Object o) {
        return Boolean.TRUE.equals(o) || "true".equals(str(o));
    }

    /** Per-signer notification result entry. */
    public static final class SummaryEntry {

        private final String signerId;
        private final boolean notified;

        /**
         * Constructs a summary entry.
         *
         * @param signerId signer id
         * @param notified whether the signer was notified
         */
        public SummaryEntry(String signerId, boolean notified) {
            this.signerId = signerId;
            this.notified = notified;
        }

        /**
         * Returns the signer id.
         *
         * @return signer id
         */
        public String signerId() {
            return signerId;
        }

        /**
         * Returns whether the signer was notified.
         *
         * @return notified flag
         */
        public boolean notified() {
            return notified;
        }
    }
}
