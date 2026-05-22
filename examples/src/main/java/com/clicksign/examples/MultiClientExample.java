package com.clicksign.examples;

import com.clicksign.ClicksignClient;
import com.clicksign.Environment;

/** Espelha {@code docs/examples/04-multi-client.md}. */
public final class MultiClientExample {

    private final ClicksignClient tenantA;
    private final ClicksignClient tenantB;

    public MultiClientExample(String tokenA, String tokenB) {
        this.tenantA = ClicksignClient.builder()
            .apiKey(tokenA)
            .environment(Environment.PRODUCTION)
            .maxRetries(2)
            .build();

        this.tenantB = ClicksignClient.builder()
            .apiKey(tokenB)
            .environment(Environment.PRODUCTION)
            .maxRetries(2)
            .build();
    }

    public ClicksignClient forTenant(String tenantId) {
        return switch (tenantId) {
            case "A" -> tenantA;
            case "B" -> tenantB;
            default -> throw new IllegalArgumentException("tenant desconhecido: " + tenantId);
        };
    }

    public static void main(String[] args) {
        MultiClientExample clients = new MultiClientExample("token-tenant-a", "token-tenant-b");
        System.out.println("clientes criados para tenants A e B");
        System.out.println("tenant A selecionado: " + clients.forTenant("A"));
    }
}
