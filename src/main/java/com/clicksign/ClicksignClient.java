package com.clicksign;

import com.clicksign.http.HttpClient;
import com.clicksign.resources.notarial.Envelope;
import com.clicksign.resources.notarial.Document;
import com.clicksign.resources.notarial.Signer;
import com.clicksign.resources.notarial.Requirement;
import com.clicksign.resources.notarial.SignatureWatcher;
import com.clicksign.resources.notarial.Event;
import com.clicksign.resources.Webhook;
import com.clicksign.resources.Folder;
import com.clicksign.resources.User;
import com.clicksign.resources.Template;
import com.clicksign.resources.TemplateField;
import com.clicksign.resources.Membership;
import com.clicksign.resources.Group;
import com.clicksign.resources.AccessControlList;
import com.clicksign.resources.EnvelopeBulkCreation;

/**
 * Entry point for the Clicksign Java SDK.
 *
 * <pre>{@code
 * ClicksignClient client = ClicksignClient.builder()
 *     .apiKey(System.getenv("CLICKSIGN_API_KEY"))
 *     .environment(Environment.SANDBOX)
 *     .build();
 *
 * Envelope envelope = client.envelopes().create(
 *     EnvelopeCreateParams.builder()
 *         .name("Contrato")
 *         .locale("pt-BR")
 *         .build()
 * );
 * }</pre>
 */
public final class ClicksignClient {

    private final HttpClient httpClient;

    private final Envelope.Service envelopes;
    private final Document.Service documents;
    private final Signer.Service signers;
    private final Requirement.Service requirements;
    private final SignatureWatcher.Service signatureWatchers;
    private final Event.Service events;
    private final Webhook.Service webhooks;
    private final Folder.Service folders;
    private final User.Service users;
    private final Template.Service templates;
    private final TemplateField.Service templateFields;
    private final Membership.Service memberships;
    private final Group.Service groups;
    private final AccessControlList.Service accessControlLists;
    private final EnvelopeBulkCreation.Service envelopeBulkCreations;

    private ClicksignClient(Builder builder) {
        ClientConfig config = ClientConfig.builder()
            .apiKey(builder.apiKey)
            .baseUrl(builder.baseUrl != null ? builder.baseUrl : builder.environment.baseUrl())
            .connectTimeoutMs(builder.connectTimeoutMs)
            .readTimeoutMs(builder.readTimeoutMs)
            .maxRetries(builder.maxRetries)
            .build();

        this.httpClient = new HttpClient(config);

        this.envelopes          = new Envelope.Service(httpClient);
        this.documents          = new Document.Service(httpClient);
        this.signers            = new Signer.Service(httpClient);
        this.requirements       = new Requirement.Service(httpClient);
        this.signatureWatchers  = new SignatureWatcher.Service(httpClient);
        this.events             = new Event.Service(httpClient);
        this.webhooks           = new Webhook.Service(httpClient);
        this.folders            = new Folder.Service(httpClient);
        this.users              = new User.Service(httpClient);
        this.templates          = new Template.Service(httpClient);
        this.templateFields     = new TemplateField.Service(httpClient);
        this.memberships        = new Membership.Service(httpClient);
        this.groups             = new Group.Service(httpClient);
        this.accessControlLists = new AccessControlList.Service(httpClient);
        this.envelopeBulkCreations = new EnvelopeBulkCreation.Service(httpClient);
    }

    public Envelope.Service envelopes()           { return envelopes; }
    public Document.Service documents()           { return documents; }
    public Signer.Service signers()               { return signers; }
    public Requirement.Service requirements()     { return requirements; }
    public SignatureWatcher.Service signatureWatchers() { return signatureWatchers; }
    public Event.Service events()                 { return events; }
    public Webhook.Service webhooks()             { return webhooks; }
    public Folder.Service folders()               { return folders; }
    public User.Service users()                   { return users; }
    public Template.Service templates()           { return templates; }
    public TemplateField.Service templateFields() { return templateFields; }
    public Membership.Service memberships()       { return memberships; }
    public Group.Service groups()                 { return groups; }
    public AccessControlList.Service accessControlLists() { return accessControlLists; }
    public EnvelopeBulkCreation.Service envelopeBulkCreations() { return envelopeBulkCreations; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;
        private Environment environment = Environment.PRODUCTION;
        private String baseUrl;
        private int connectTimeoutMs = 2_000;
        private int readTimeoutMs    = 10_000;
        private int maxRetries       = 0;

        private Builder() {}

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        /** Overrides environment URL — useful for tests or custom proxies. */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public ClicksignClient build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("apiKey is required");
            }
            return new ClicksignClient(this);
        }
    }
}
