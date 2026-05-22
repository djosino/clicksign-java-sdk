package com.clicksign.resources.types;

import com.clicksign.resources.notarial.CommunicateEvents;
import com.clicksign.resources.notarial.NotificationChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypesTest {

    @Test
    void metadataRoundTrip() {
        Metadata metadata = Metadata.builder()
            .put("contract_id", "42")
            .put("tier", "gold")
            .build();

        assertEquals("42", Metadata.fromMap(metadata.toMap()).get("contract_id"));
    }

    @Test
    void documentTemplateSerializesIdAndFields() {
        DocumentTemplate template = DocumentTemplate.withFields("tpl-1",
            java.util.Map.of("campo", "valor"));

        assertEquals("tpl-1", template.toMap().get("id"));
        assertEquals("valor", template.toMap().get("campo"));
    }

    @Test
    void apiStringEnumParsesAndRejectsUnknown() {
        assertEquals(EnvelopeStatus.DRAFT, ApiStringEnum.tryParse(EnvelopeStatus.class, "draft"));
        assertNull(ApiStringEnum.tryParse(RequirementRole.class, "unknown_role"));
        assertEquals(RequirementAuth.EMAIL, ApiStringEnum.require(RequirementAuth.class, "email"));
    }

    @Test
    void emailCustomizationToMap() {
        EmailCustomization customization = EmailCustomization.builder()
            .subject("Assinatura pendente")
            .build();

        assertEquals("Assinatura pendente", customization.toMap().get("subject"));
    }

    @Test
    void communicateEventsIntegratesWithNotificationChannel() {
        assertEquals("whatsapp",
            CommunicateEvents.builder()
                .signatureRequest(NotificationChannel.WHATSAPP)
                .build()
                .toMap()
                .get("signature_request"));
    }
}
