package com.clicksign.examples;

/** Utilitário compartilhado pelos exemplos executáveis. */
public final class ExampleSupport {

    private ExampleSupport() {}

    /** Retorna a API key ou encerra o processo com mensagem amigável. */
    public static String requireApiKey() {
        String key = System.getenv("CLICKSIGN_API_KEY");
        if (key == null || key.isBlank()) {
            System.err.println("Defina CLICKSIGN_API_KEY para executar este exemplo.");
            System.exit(0);
        }
        return key;
    }
}
