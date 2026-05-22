package com.clicksign.integration;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;
import com.clicksign.resources.notarial.Envelope;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Optional live tests against Clicksign sandbox.
 * Run with: {@code CLICKSIGN_API_KEY=... ./gradlew test --tests "*.SandboxIntegrationTest" -DincludeIntegration}
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "CLICKSIGN_API_KEY", matches = ".+")
class SandboxIntegrationTest {

    @Test
    void listEnvelopesAgainstSandbox() {
        ClicksignClient client = ClicksignClient.builder()
            .apiKey(System.getenv("CLICKSIGN_API_KEY"))
            .environment(Environment.SANDBOX)
            .build();

        assertDoesNotThrow(() -> {
            java.util.List<Envelope> envelopes = client.envelopes().list();
            assertNotNull(envelopes);
        });
    }
}
