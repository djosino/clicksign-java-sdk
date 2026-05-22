package com.clicksign.resources.notarial;

import com.clicksign.resources.types.ApiStringEnum;
import com.clicksign.resources.types.DeadlinePartialSignatureAction;
import com.clicksign.resources.types.EnvelopeLocale;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

/** Client-side validation for envelope create/update parameters. */
final class EnvelopeValidation {

    private static final Set<Integer> ALLOWED_REMIND_INTERVALS = Set.of(1, 2, 3, 7, 14);
    private static final int MAX_DEADLINE_DAYS = 90;
    private static final int MAX_DEFAULT_SUBJECT_LENGTH = 100;

    private EnvelopeValidation() {}

    static void validateOptionalFields(
            String locale,
            Integer remindInterval,
            String deadlineAt,
            String deadlinePartialSignatureAction,
            String defaultSubject) {

        if (locale != null && ApiStringEnum.tryParse(EnvelopeLocale.class, locale) == null) {
            throw new IllegalArgumentException("locale must be one of: pt-BR, en-US");
        }
        if (remindInterval != null && !ALLOWED_REMIND_INTERVALS.contains(remindInterval)) {
            throw new IllegalArgumentException("remindInterval must be one of: 1, 2, 3, 7, 14");
        }
        if (deadlineAt != null) {
            validateDeadlineAt(deadlineAt);
        }
        if (deadlinePartialSignatureAction != null
            && ApiStringEnum.tryParse(DeadlinePartialSignatureAction.class, deadlinePartialSignatureAction) == null) {
            throw new IllegalArgumentException(
                "deadlinePartialSignatureAction must be one of: closed, canceled");
        }
        if (defaultSubject != null && defaultSubject.length() > MAX_DEFAULT_SUBJECT_LENGTH) {
            throw new IllegalArgumentException(
                "defaultSubject must be at most " + MAX_DEFAULT_SUBJECT_LENGTH + " characters");
        }
    }

    private static void validateDeadlineAt(String deadlineAt) {
        OffsetDateTime parsed;
        try {
            parsed = OffsetDateTime.parse(deadlineAt);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("deadlineAt must be a valid ISO 8601 datetime", e);
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (!parsed.isAfter(now)) {
            throw new IllegalArgumentException("deadlineAt must be in the future");
        }
        if (parsed.isAfter(now.plusDays(MAX_DEADLINE_DAYS))) {
            throw new IllegalArgumentException("deadlineAt must be at most " + MAX_DEADLINE_DAYS + " days from now");
        }
    }
}
