package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.resources.notarial.BulkRequirement;
import com.clicksign.resources.types.RequirementAuth;
import com.clicksign.resources.types.RequirementRole;
import com.clicksign.resources.types.RubricateKind;

/** Espelha {@code docs/examples/02-bulk-requirements.md}. */
public final class BulkRequirementsExample {

    public static void main(String[] args) {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(ExampleSupport.requireApiKey())
            .environment(Environment.SANDBOX)
            .build();

        String envelopeId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String signerId   = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
        String documentId = "cccccccc-cccc-cccc-cccc-cccccccccccc";

        BulkRequirement.Response response = client.bulkRequirements().create(
            envelopeId,
            ops -> ops
                .addAgree(signerId, documentId, RequirementRole.SIGN)
                .addProvideEvidence(signerId, documentId, RequirementAuth.EMAIL)
                .addRubricate(signerId, documentId, "1-3", null, RubricateKind.INITIALS.apiValue()));

        if (response.isSuccess()) {
            response.requirements().forEach(r ->
                System.out.println("requirement " + r.id() + " action=" + r.action()));
        } else {
            for (BulkRequirement.OperationResult slot : response.failures()) {
                System.err.println("falha no índice " + slot.index() + " op=" + slot.op());
                slot.errors().forEach(System.err::println);
            }
        }

        client.bulkRequirements().create(envelopeId,
            ops -> ops.remove("dddddddd-dddd-dddd-dddd-dddddddddddd"));
    }
}
