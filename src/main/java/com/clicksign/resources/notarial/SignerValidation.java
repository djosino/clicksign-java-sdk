package com.clicksign.resources.notarial;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/** Client-side validation for signer create parameters. */
final class SignerValidation {

    private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private SignerValidation() {}

    static void validateName(String name) {
        if (name.matches(".*\\d.*")) {
            throw new IllegalArgumentException("name must not contain numbers");
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("name must include at least first and last name");
        }
    }

    static void validateOptionalFields(
            Boolean hasDocumentation,
            String documentation,
            String birthday,
            String phoneNumber,
            Map<String, Object> communicateEvents) {

        if (Boolean.FALSE.equals(hasDocumentation)) {
            if (documentation != null && !documentation.isBlank()) {
                throw new IllegalArgumentException(
                    "documentation must not be set when hasDocumentation is false");
            }
            if (birthday != null && !birthday.isBlank()) {
                throw new IllegalArgumentException(
                    "birthday must not be set when hasDocumentation is false");
            }
        }
        if (birthday != null && !birthday.isBlank()) {
            try {
                LocalDate.parse(birthday, BIRTHDAY_FORMAT);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("birthday must use format AAAA-MM-DD", e);
            }
        }
        if (communicateEvents != null && requiresPhoneNumber(communicateEvents)
            && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new IllegalArgumentException(
                "phoneNumber is required when communicate_events uses sms or whatsapp");
        }
    }

    private static boolean requiresPhoneNumber(Map<String, Object> communicateEvents) {
        for (String channel : new String[] {"signature_request", "signature_reminder", "document_signed"}) {
            Object value = communicateEvents.get(channel);
            if (value == null) continue;
            String s = value.toString();
            if ("sms".equals(s) || "whatsapp".equals(s)) return true;
        }
        return false;
    }
}
