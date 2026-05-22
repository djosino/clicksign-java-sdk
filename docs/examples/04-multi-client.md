# Múltiplos clientes (multi-tenant)

**Quando usar:** SaaS com várias contas Clicksign, cada uma com seu access token, ou separação sandbox/produção na mesma JVM.

## Código

```java
import com.clicksign.ClicksignClient;
import com.clicksign.Environment;

public final class ClicksignClients {

    private final ClicksignClient tenantA;
    private final ClicksignClient tenantB;

    public ClicksignClients(String tokenA, String tokenB) {
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
            default -> throw new IllegalArgumentException("tenant desconhecido");
        };
    }
}
```

### Spring (injeção)

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClicksignConfig {

    @Bean
    ClicksignClient clicksignSandbox(
            @org.springframework.beans.factory.annotation.Value("${clicksign.sandbox.token}") String token) {
        return ClicksignClient.builder()
            .apiKey(token)
            .environment(com.clicksign.Environment.SANDBOX)
            .build();
    }
}
```

Para múltiplos tenants dinâmicos, prefira factory com cache por `tenantId` em vez de um `@Bean` singleton único.

## O que está acontecendo

- Não há `Services.use { }` como no Ruby — cada `ClicksignClient` carrega seu próprio `HttpClient` e token
- Instâncias são imutáveis e podem ser compartilhadas entre threads
- `baseUrl()` no builder permite apontar para WireMock em testes de integração

## Erros comuns

- Singleton global com token de um tenant servindo todos — vazamento de dados entre clientes
- Reutilizar o mesmo builder mutável entre tenants — crie um `build()` por conta
- Misturar `Environment.SANDBOX` com token de produção

Não há cliente async nativo — para paralelismo, use um `ExecutorService` com um `ClicksignClient` por tenant.
