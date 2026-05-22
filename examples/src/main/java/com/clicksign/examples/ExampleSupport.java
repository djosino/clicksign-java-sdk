package com.clicksign.examples;

import com.clicksign.Environment;

/** Utilitário compartilhado pelos exemplos executáveis. */
public final class ExampleSupport {

    private ExampleSupport() {}

    /**
     * Ambiente da API. Padrão: {@link Environment#SANDBOX}.
     * Sobrescreva com {@code CLICKSIGN_ENVIRONMENT=sandbox} ou {@code production}.
     */
    public static Environment environment() {
        String env = System.getenv("CLICKSIGN_ENVIRONMENT");
        if (env == null || env.isBlank()) {
            return Environment.SANDBOX;
        }
        String normalized = env.toLowerCase();
        if ("sandbox".equals(normalized)) {
            return Environment.SANDBOX;
        }
        if ("production".equals(normalized) || "prod".equals(normalized)) {
            return Environment.PRODUCTION;
        }
        throw new IllegalArgumentException(
            "CLICKSIGN_ENVIRONMENT inválido: use sandbox ou production");
    }

    /** Retorna a API key ou encerra o processo com mensagem amigável. */
    public static String requireApiKey() {
        String key = System.getenv("CLICKSIGN_API_KEY");
        if (key == null || key.isBlank()) {
            System.err.println("Defina CLICKSIGN_API_KEY para executar este exemplo.");
            System.exit(0);
        }
        return key;
    }

    /** PDF em branco público — uso em exemplos com {@code content_url}. */
    public static final String SAMPLE_PDF_URL =
        "https://mag.wcoomd.org/uploads/2018/05/blank.pdf";
}
