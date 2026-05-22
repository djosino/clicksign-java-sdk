package com.clicksign;

/** Shared JSON:API response fixtures for tests. */
public final class JsonApiFixtures {

    public static final String BASE_URL = "http://localhost:8089";

    private JsonApiFixtures() {}

    public static String envelope(String id, String name, String status) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"envelopes\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"status\":\"" + status + "\"," +
            "\"auto_close\":false,\"locale\":\"pt-BR\",\"block_after_refusal\":false," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String envelopeWithDeadlines(String id, String name, String status,
            String deadlineAt, String deadlineAction, int remindInterval) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"envelopes\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"status\":\"" + status + "\"," +
            "\"auto_close\":false,\"locale\":\"pt-BR\",\"block_after_refusal\":false," +
            "\"deadline_at\":\"" + deadlineAt + "\"," +
            "\"deadline_partial_signature_action\":\"" + deadlineAction + "\"," +
            "\"remind_interval\":" + remindInterval + "," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String envelopeList(String... envelopes) {
        return resourceList(envelopes);
    }

    public static String envelopeListWithLinks(String nextUrl, String... envelopes) {
        String list = envelopeList(envelopes);
        String links = nextUrl != null
            ? ",\"links\":{\"next\":\"" + nextUrl + "\"}"
            : ",\"links\":{\"next\":null}";
        return list.substring(0, list.length() - 1) + links + "}";
    }

    public static String documentListWithNext(String nextUrl, String... documents) {
        String list = resourceList(documents);
        return list.substring(0, list.length() - 1) + ",\"links\":{\"next\":\"" + nextUrl + "\"}}";
    }

    public static String signerListForEnvelope(String envelopeId, String... signers) {
        return resourceList(signers);
    }

    public static String document(String id, String filename, String envelopeId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"documents\",\"attributes\":{" +
            "\"filename\":\"" + filename + "\",\"status\":\"draft\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"" + envelopeId + "\"}}}}}";
    }

    public static String documentList(String... documents) {
        return resourceList(documents);
    }

    public static String signer(String id, String name, String email, String envelopeId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"signers\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"email\":\"" + email + "\"," +
            "\"refusable\":false,\"has_documentation\":false," +
            "\"location_required_enabled\":false," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"" + envelopeId + "\"}}}}}";
    }

    public static String signerWithCommunicateEvents(String id, String name, String email, String envelopeId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"signers\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"email\":\"" + email + "\"," +
            "\"refusable\":false,\"has_documentation\":false," +
            "\"location_required_enabled\":false," +
            "\"group\":1," +
            "\"communicate_events\":{\"signature_request\":\"email\",\"signature_reminder\":\"none\"," +
            "\"document_signed\":\"whatsapp\"}," +
            "\"signature_host\":{\"name\":\"Host Name\",\"email\":\"host@example.com\"," +
            "\"communicate_events\":{\"signature_request\":\"email\"}}," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"" + envelopeId + "\"}}}}}";
    }

    public static String signerList(String... signers) {
        return resourceList(signers);
    }

    public static String requirement(String id, String action, String envelopeId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"requirements\",\"attributes\":{" +
            "\"action\":\"" + action + "\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"" + envelopeId + "\"}}}}}";
    }

    public static String requirementList(String... requirements) {
        return resourceList(requirements);
    }

    public static String signatureWatcher(String id, String email, String envelopeId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"signature_watchers\",\"attributes\":{" +
            "\"email\":\"" + email + "\",\"kind\":\"all_steps\"," +
            "\"attach_documents_enabled\":true," +
            "\"communicate_events\":{\"signature_watcher_document_sent\":\"email\"}," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"" + envelopeId + "\"}}}}}";
    }

    public static String signatureWatcherList(String... watchers) {
        return resourceList(watchers);
    }

    public static String event(String id, String name) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"events\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"data\":{},\"created\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String eventList(String... events) {
        return resourceList(events);
    }

    public static String template(String id, String name) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"templates\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"color\":\"#FF0000\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String templateList(String... templates) {
        return resourceList(templates);
    }

    public static String user(String id, String name, String email) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"users\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"email\":\"" + email + "\"," +
            "\"phone_number\":\"11987654321\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String userList(String... users) {
        return resourceList(users);
    }

    public static String folder(String id, String name) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"folders\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"path\":\"/" + name + "\",\"in_root\":true," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String folderWithParent(String id, String name, String parentFolderId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"folders\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"path\":\"/parent/" + name + "\",\"in_root\":false," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"folder\":{\"data\":{\"type\":\"folders\",\"id\":\"" + parentFolderId + "\"}}}}}";
    }

    public static String folderList(String... folders) {
        return resourceList(folders);
    }

    public static String group(String id, String name) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"groups\",\"attributes\":{" +
            "\"name\":\"" + name + "\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String groupList(String... groups) {
        return resourceList(groups);
    }

    public static String accessControlList(String id, String folderId, String groupId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"access_control_lists\",\"relationships\":{" +
            "\"folder\":{\"data\":{\"type\":\"folders\",\"id\":\"" + folderId + "\"}}," +
            "\"group\":{\"data\":{\"type\":\"groups\",\"id\":\"" + groupId + "\"}}}}}";
    }

    public static String envelopeBulkCreation(String id, String jobId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"envelope_bulk_creations\",\"attributes\":{" +
            "\"job_id\":\"" + jobId + "\",\"enqueued_at\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String templateField(String id, String name, String templateId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"template_fields\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"kind\":\"text\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"template\":{\"data\":{\"type\":\"templates\",\"id\":\"" + templateId + "\"}}}}}";
    }

    public static String templateFieldList(String... fields) {
        return resourceList(fields);
    }

    public static String webhookList(String... webhooks) {
        return resourceList(webhooks);
    }

    public static String membershipList(String... memberships) {
        return resourceList(memberships);
    }

    public static String requirementWithRelations(String id, String action, String envelopeId,
            String documentId, String signerId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"requirements\",\"attributes\":{" +
            "\"action\":\"" + action + "\",\"role\":\"sign\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{" +
            "\"envelope\":{\"data\":{\"type\":\"envelopes\",\"id\":\"" + envelopeId + "\"}}," +
            "\"document\":{\"data\":{\"type\":\"documents\",\"id\":\"" + documentId + "\"}}," +
            "\"signer\":{\"data\":{\"type\":\"signers\",\"id\":\"" + signerId + "\"}}}}}";
    }

    public static String membership(String id, String role, String userId) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"memberships\",\"attributes\":{" +
            "\"role\":\"" + role + "\"," +
            "\"consumption_accessible\":true,\"tracking_accessible\":false," +
            "\"folder_management_accessible\":true," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}," +
            "\"relationships\":{\"user\":{\"data\":{\"type\":\"users\",\"id\":\"" + userId + "\"}}}}}";
    }

    public static String webhook(String id, String endpoint, String status) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"webhooks\",\"attributes\":{" +
            "\"endpoint\":\"" + endpoint + "\",\"status\":\"" + status + "\"," +
            "\"events\":[\"sign\",\"close\"],\"secret\":\"sec123\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String atomicResults(String requirementId, String action) {
        return "{\"atomic:results\":[{\"data\":{\"id\":\"" + requirementId + "\"," +
            "\"type\":\"requirements\",\"attributes\":{\"action\":\"" + action + "\"}," +
            "\"relationships\":{}}}]}";
    }

    public static String atomicResultsWithError(String detail) {
        return "{\"atomic:results\":[{\"errors\":[{\"detail\":\"" + detail + "\",\"status\":\"404\"}]}]}";
    }

    public static String envelopeNotification(String id, String signerId1, String signerId2) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"notifications\",\"attributes\":{" +
            "\"message\":\"Mensagem de teste\"," +
            "\"summary\":[" +
            "{\"signer_id\":\"" + signerId1 + "\",\"notified\":true}," +
            "{\"signer_id\":\"" + signerId2 + "\",\"notified\":true}" +
            "],\"created\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String acceptanceTermWhatsapp(String id, String status, String title) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"acceptance_term_whatsapps\",\"attributes\":{" +
            "\"title\":\"" + title + "\",\"message\":\"Termo de aceite\"," +
            "\"signer_name\":\"João Silva\",\"signer_phone\":\"11987654321\"," +
            "\"sender_name_option\":\"account_name\",\"sender_phone\":null," +
            "\"status\":\"" + status + "\",\"status_flow\":\"introduction\"," +
            "\"sent_at\":null," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String acceptanceTermWhatsappList(String... items) {
        return resourceList(items);
    }

    public static String autoSignatureTerm(String id, String name, String email) {
        return "{\"data\":{\"id\":\"" + id + "\",\"type\":\"auto_signature_terms\",\"attributes\":{" +
            "\"name\":\"" + name + "\",\"email\":\"" + email + "\"," +
            "\"documentation\":\"123.456.789-10\",\"birthday\":\"1990-01-01\"," +
            "\"created\":\"2026-01-01T00:00:00.000-03:00\"," +
            "\"modified\":\"2026-01-01T00:00:00.000-03:00\"}}}";
    }

    public static String errorBody(String detail) {
        return "{\"errors\":[{\"detail\":\"" + detail + "\"}]}";
    }

    private static String resourceList(String... singles) {
        StringBuilder sb = new StringBuilder("{\"data\":[");
        for (int i = 0; i < singles.length; i++) {
            if (i > 0) sb.append(',');
            String single = singles[i];
            int start = single.indexOf("\"data\":") + 7;
            int end   = single.lastIndexOf('}');
            sb.append(single, start, end);
        }
        sb.append("]}");
        return sb.toString();
    }
}
