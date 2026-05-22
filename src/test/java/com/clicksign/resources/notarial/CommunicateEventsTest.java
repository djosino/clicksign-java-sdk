package com.clicksign.resources.notarial;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommunicateEventsTest {

    @Test
    void toMapSerializesChannels() {
        CommunicateEvents events = CommunicateEvents.builder()
            .signatureRequest(NotificationChannel.EMAIL)
            .documentSigned(NotificationChannel.WHATSAPP)
            .build();

        Map<String, Object> map = events.toMap();
        assertEquals("email", map.get("signature_request"));
        assertEquals("whatsapp", map.get("document_signed"));
        assertFalse(map.containsKey("signature_reminder"));
    }

    @Test
    void fromMapRoundTrips() {
        CommunicateEvents original = CommunicateEvents.builder()
            .signatureReminder(NotificationChannel.NONE)
            .build();

        CommunicateEvents parsed = CommunicateEvents.fromMap(original.toMap());
        assertEquals(NotificationChannel.NONE, parsed.signatureReminder());
    }

    @Test
    void signerCreateAcceptsTypedCommunicateEvents() {
        assertDoesNotThrow(() ->
            Signer.CreateParams.builder()
                .envelopeId("env-1")
                .name("João Silva")
                .email("joao@example.com")
                .phoneNumber("11987654321")
                .communicateEvents(CommunicateEvents.builder()
                    .signatureRequest(NotificationChannel.WHATSAPP)
                    .build())
                .build());
    }

    @Test
    void rejectsUnknownChannelOnParse() {
        assertThrows(IllegalArgumentException.class, () ->
            CommunicateEvents.fromMap(Map.of("signature_request", "telegram")));
    }
}
