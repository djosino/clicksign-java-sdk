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

    @Test
    void documentDuplicateToMap() {
        assertEquals("doc-1", DocumentDuplicate.of("doc-1").toMap().get("id"));
        assertThrows(IllegalArgumentException.class, () -> DocumentDuplicate.of(" "));
    }

    @Test
    void autoSignatureSignerBuilder() {
        AutoSignatureSigner signer = AutoSignatureSigner.builder()
            .name("João Silva")
            .email("joao@example.com")
            .documentation("123.456.789-10")
            .birthday("1990-01-01")
            .build();

        assertEquals("João Silva", signer.toMap().get("name"));
    }

    @Test
    void apiStringEnumRequireThrowsOnUnknown() {
        assertThrows(IllegalArgumentException.class,
            () -> ApiStringEnum.require(RequirementAction.class, "invalid"));
    }

    @Test
    void enumsExposeApiValues() {
        assertEquals("draft", EnvelopeStatus.DRAFT.apiValue());
        assertEquals("running", DocumentStatus.RUNNING.apiValue());
        assertEquals("pt-BR", EnvelopeLocale.PT_BR.apiValue());
        assertEquals("closed", DeadlinePartialSignatureAction.CLOSED.apiValue());
        assertEquals("agree", RequirementAction.AGREE.apiValue());
        assertEquals("sign", RequirementRole.SIGN.apiValue());
        assertEquals("email", RequirementAuth.EMAIL.apiValue());
        assertEquals("initials", RubricateKind.INITIALS.apiValue());
        assertEquals("all_steps", SignatureWatcherKind.ALL_STEPS.apiValue());
        assertEquals("sent", AcceptanceTermStatus.SENT.apiValue());
        assertEquals("account_name", SenderNameOption.ACCOUNT_NAME.apiValue());
        assertEquals("token_email", EventCustomKind.TOKEN_EMAIL.apiValue());
        assertEquals("sign", WebhookEventType.SIGN.apiValue());
        assertEquals("admin", MembershipRole.ADMIN.apiValue());
        assertEquals("member", MembershipRole.MEMBER.apiValue());
    }

    @Test
    void membershipRoleParses() {
        assertEquals(MembershipRole.ADMIN, ApiStringEnum.tryParse(MembershipRole.class, "admin"));
        assertNull(ApiStringEnum.tryParse(MembershipRole.class, "owner"));
    }
}
