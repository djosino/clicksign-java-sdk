package com.clicksign;

import com.clicksign.http.HttpClient;
import com.clicksign.instrumentation.ErrorEvent;
import com.clicksign.instrumentation.Instrumentation;
import com.clicksign.instrumentation.RequestEvent;
import com.clicksign.instrumentation.RetryEvent;
import com.clicksign.jsonapi.BulkOperationsClient;
import com.clicksign.resources.notarial.BulkRequirement;
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
import com.clicksign.resources.AcceptanceTermWhatsapp;
import com.clicksign.resources.AutoSignatureTerm;

import java.util.function.Consumer;

/**
 * Entry point for the Clicksign Java SDK.
 *
 * <pre>{@code
 * ClicksignClient client = ClicksignClient.builder()
 *     .apiKey(System.getenv("CLICKSIGN_API_KEY"))
 *     .environment(Environment.SANDBOX)
 *     .onRequest(e -> System.out.printf("%s %s %d%n", e.method(), e.path(), e.status()))
 *     .build();
 *
 * Envelope envelope = client.envelopes().create(
 *     Envelope.CreateParams.builder()
 *         .name("Contrato")
 *         .locale("pt-BR")
 *         .build()
 * );
 * }</pre>
 */
public final class ClicksignClient {

    private final HttpClient httpClient;
    private final BulkOperationsClient bulkClient;

    private final Envelope.Service envelopes;
    private final Document.Service documents;
    private final Signer.Service signers;
    private final Requirement.Service requirements;
    private final SignatureWatcher.Service signatureWatchers;
    private final Event.Service events;
    private final BulkRequirement.Service bulkRequirements;
    private final Webhook.Service webhooks;
    private final Folder.Service folders;
    private final User.Service users;
    private final Template.Service templates;
    private final TemplateField.Service templateFields;
    private final Membership.Service memberships;
    private final Group.Service groups;
    private final AccessControlList.Service accessControlLists;
    private final EnvelopeBulkCreation.Service envelopeBulkCreations;
    private final AcceptanceTermWhatsapp.Service acceptanceTermWhatsapps;
    private final AutoSignatureTerm.Service autoSignatureTerms;

    private ClicksignClient(Builder builder) {
        ClientConfig config = ClientConfig.builder()
            .apiKey(builder.apiKey)
            .baseUrl(builder.baseUrl != null ? builder.baseUrl : builder.environment.baseUrl())
            .connectTimeoutMs(builder.connectTimeoutMs)
            .readTimeoutMs(builder.readTimeoutMs)
            .maxRetries(builder.maxRetries)
            .build();

        Instrumentation instrumentation = builder.instrumentation;

        this.httpClient = new HttpClient(config, instrumentation);
        this.bulkClient = new BulkOperationsClient(config);

        this.envelopes             = new Envelope.Service(httpClient);
        this.documents             = new Document.Service(httpClient);
        this.signers               = new Signer.Service(httpClient);
        this.requirements          = new Requirement.Service(httpClient);
        this.signatureWatchers     = new SignatureWatcher.Service(httpClient);
        this.events                = new Event.Service(httpClient);
        this.bulkRequirements      = new BulkRequirement.Service(bulkClient);
        this.webhooks              = new Webhook.Service(httpClient);
        this.folders               = new Folder.Service(httpClient);
        this.users                 = new User.Service(httpClient);
        this.templates             = new Template.Service(httpClient);
        this.templateFields        = new TemplateField.Service(httpClient);
        this.memberships           = new Membership.Service(httpClient);
        this.groups                = new Group.Service(httpClient);
        this.accessControlLists    = new AccessControlList.Service(httpClient);
        this.envelopeBulkCreations = new EnvelopeBulkCreation.Service(httpClient);
        this.acceptanceTermWhatsapps = new AcceptanceTermWhatsapp.Service(httpClient);
        this.autoSignatureTerms      = new AutoSignatureTerm.Service(httpClient);
    }

    /**
     * Returns the envelopes.
     *
     * @return envelopes
     */
    public Envelope.Service envelopes() {
        return envelopes;
    }

    /**
     * Returns the documents.
     *
     * @return documents
     */
    public Document.Service documents() {
        return documents;
    }

    /**
     * Returns the signers.
     *
     * @return signers
     */
    public Signer.Service signers() {
        return signers;
    }

    /**
     * Returns the requirements.
     *
     * @return requirements
     */
    public Requirement.Service requirements() {
        return requirements;
    }

    /**
     * Returns the signature watchers.
     *
     * @return signature watchers
     */
    public SignatureWatcher.Service signatureWatchers() {
        return signatureWatchers;
    }

    /**
     * Returns the events.
     *
     * @return events
     */
    public Event.Service events() {
        return events;
    }

    /**
     * Returns the bulk requirements.
     *
     * @return bulk requirements
     */
    public BulkRequirement.Service bulkRequirements() {
        return bulkRequirements;
    }

    /**
     * Returns the webhooks.
     *
     * @return webhooks
     */
    public Webhook.Service webhooks() {
        return webhooks;
    }

    /**
     * Returns the folders.
     *
     * @return folders
     */
    public Folder.Service folders() {
        return folders;
    }

    /**
     * Returns the users.
     *
     * @return users
     */
    public User.Service users() {
        return users;
    }

    /**
     * Returns the templates.
     *
     * @return templates
     */
    public Template.Service templates() {
        return templates;
    }

    /**
     * Returns the template fields.
     *
     * @return template fields
     */
    public TemplateField.Service templateFields() {
        return templateFields;
    }

    /**
     * Returns the memberships.
     *
     * @return memberships
     */
    public Membership.Service memberships() {
        return memberships;
    }

    /**
     * Returns the groups.
     *
     * @return groups
     */
    public Group.Service groups() {
        return groups;
    }

    /**
     * Returns the access control lists.
     *
     * @return access control lists
     */
    public AccessControlList.Service accessControlLists() {
        return accessControlLists;
    }

    /**
     * Returns the envelope bulk creations.
     *
     * @return envelope bulk creations
     */
    public EnvelopeBulkCreation.Service envelopeBulkCreations() {
        return envelopeBulkCreations;
    }

    /**
     * Returns the acceptance term whatsapps.
     *
     * @return acceptance term whatsapps
     */
    public AcceptanceTermWhatsapp.Service acceptanceTermWhatsapps() {
        return acceptanceTermWhatsapps;
    }

    /**
     * Returns the auto signature terms.
     *
     * @return auto signature terms
     */
    public AutoSignatureTerm.Service autoSignatureTerms() {
        return autoSignatureTerms;
    }

    /**
     * Returns a new builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder. */
    public static final class Builder {

        private String apiKey;
        private Environment environment    = Environment.PRODUCTION;
        private String baseUrl;
        private int connectTimeoutMs       = 2_000;
        private int readTimeoutMs          = 10_000;
        private int maxRetries             = 0;
        private final Instrumentation instrumentation = new Instrumentation();

        private Builder() {}

        /**
         * Sets api key.
         *
         * @param apiKey value
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets environment.
         *
         * @param environment value
         * @return this builder
         */
        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        /**
         * Overrides environment URL — useful for tests or custom proxies.
         *
         * @param baseUrl base URL to use instead of the environment default
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets connect timeout ms.
         *
         * @param ms value
         * @return this builder
         */
        public Builder connectTimeoutMs(int ms) {
            this.connectTimeoutMs = ms;
            return this;
        }

        /**
         * Sets read timeout ms.
         *
         * @param ms value
         * @return this builder
         */
        public Builder readTimeoutMs(int ms) {
            this.readTimeoutMs = ms;
            return this;
        }

        /**
         * Sets max retries.
         *
         * @param maxRetries value
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets on request.
         *
         * @param listener value
         * @return this builder
         */
        public Builder onRequest(Consumer<RequestEvent> listener) {
            instrumentation.onRequest(listener);
            return this;
        }

        /**
         * Sets on retry.
         *
         * @param listener value
         * @return this builder
         */
        public Builder onRetry(Consumer<RetryEvent> listener) {
            instrumentation.onRetry(listener);
            return this;
        }

        /**
         * Sets on error.
         *
         * @param listener value
         * @return this builder
         */
        public Builder onError(Consumer<ErrorEvent> listener) {
            instrumentation.onError(listener);
            return this;
        }

        /**
         * Returns the build.
         *
         * @return build
         */
        public ClicksignClient build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("apiKey is required");
            }
            return new ClicksignClient(this);
        }
    }
}
