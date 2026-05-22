package com.clicksign.resources.notarial;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvelopeValidationTest {

    @Test
    void createRejectsInvalidLocale() {
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .locale("es-ES")
                .build());
    }

    @Test
    void createRejectsInvalidRemindInterval() {
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .remindInterval(5)
                .build());
    }

    @Test
    void createAcceptsValidRemindInterval() {
        assertDoesNotThrow(() ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .remindInterval(7)
                .build());
    }

    @Test
    void createRejectsDeadlineInPast() {
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .deadlineAt("2020-01-01T00:00:00-03:00")
                .build());
    }

    @Test
    void createRejectsDeadlineBeyond90Days() {
        String tooFar = OffsetDateTime.now().plusDays(91).toString();
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .deadlineAt(tooFar)
                .build());
    }

    @Test
    void createRejectsInvalidDeadlineAction() {
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .deadlinePartialSignatureAction("invalid")
                .build());
    }

    @Test
    void createRejectsDefaultSubjectTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.CreateParams.builder()
                .name("Contrato Teste")
                .defaultSubject("x".repeat(101))
                .build());
    }

    @Test
    void updateRejectsInvalidLocale() {
        assertThrows(IllegalArgumentException.class, () ->
            Envelope.UpdateParams.builder().locale("fr-FR").build());
    }
}
