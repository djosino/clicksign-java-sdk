package com.clicksign.resources;

import com.clicksign.http.HttpClient;
import com.clicksign.jsonapi.JsonApiParser;
import com.clicksign.jsonapi.JsonApiSerializer;
import com.clicksign.resources.types.AutoSignatureSigner;

import java.util.*;

/** Automatic signature authorization term for a signer. */
public final class AutoSignatureTerm {

    private final String id;
    private final String name;
    private final String email;
    private final String documentation;
    private final String birthday;
    private final String createdAt;
    private final String modifiedAt;

    private AutoSignatureTerm(JsonApiParser.ResourceObject obj) {
        Map<String, Object> a = obj.attributes();
        this.id            = obj.id();
        this.name          = str(a.get("name"));
        this.email         = str(a.get("email"));
        this.documentation = str(a.get("documentation"));
        this.birthday      = str(a.get("birthday"));
        this.createdAt     = str(a.get("created"));
        this.modifiedAt    = str(a.get("modified"));
    }

    public String id()            { return id; }
    public String name()          { return name; }
    public String email()         { return email; }
    public String documentation() { return documentation; }
    public String birthday()      { return birthday; }
    public String createdAt()     { return createdAt; }
    public String modifiedAt()    { return modifiedAt; }

    @Override
    public String toString() {
        return "AutoSignatureTerm{id='" + id + "', email='" + email + "'}";
    }

    private static String str(Object o) { return o != null ? o.toString() : null; }

    // ── Service ─────────────────────────────────────────────────────────────

    public static final class Service {

        private static final String ENDPOINT = "/auto_signature/terms";
        private final HttpClient http;

        public Service(HttpClient http) { this.http = http; }

        public AutoSignatureTerm create(CreateParams params) {
            String body = JsonApiSerializer.dump("auto_signature_terms", null, params.toAttributes(), null);
            String raw  = http.post(ENDPOINT, body);
            return new AutoSignatureTerm(JsonApiParser.parse(raw).firstData());
        }
    }

    // ── CreateParams ─────────────────────────────────────────────────────────

    public static final class CreateParams {

        private final String signerName;
        private final String signerEmail;
        private final String signerDocumentation;
        private final String signerBirthday;
        private final String apiEmail;
        private final String adminEmail;

        private CreateParams(Builder b) {
            this.signerName          = b.signerName;
            this.signerEmail         = b.signerEmail;
            this.signerDocumentation = b.signerDocumentation;
            this.signerBirthday      = b.signerBirthday;
            this.apiEmail            = b.apiEmail;
            this.adminEmail          = b.adminEmail;
        }

        Map<String, Object> toAttributes() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("signer", AutoSignatureSigner.builder()
                .name(signerName)
                .email(signerEmail)
                .documentation(signerDocumentation)
                .birthday(signerBirthday)
                .build()
                .toMap());
            m.put("api_email", apiEmail);
            m.put("admin_email", adminEmail);
            return m;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String signerName;
            private String signerEmail;
            private String signerDocumentation;
            private String signerBirthday;
            private String apiEmail;
            private String adminEmail;

            private Builder() {}

            public Builder signer(AutoSignatureSigner signer) {
                return signerName(signer.name())
                    .signerEmail(signer.email())
                    .signerDocumentation(signer.documentation())
                    .signerBirthday(signer.birthday());
            }

            public Builder signerName(String v)          { this.signerName = v; return this; }
            public Builder signerEmail(String v)         { this.signerEmail = v; return this; }
            public Builder signerDocumentation(String v) { this.signerDocumentation = v; return this; }
            public Builder signerBirthday(String v)      { this.signerBirthday = v; return this; }
            public Builder apiEmail(String v)            { this.apiEmail = v; return this; }
            public Builder adminEmail(String v)          { this.adminEmail = v; return this; }

            public CreateParams build() {
                if (signerName == null || signerName.isBlank()) throw new IllegalArgumentException("signerName is required");
                if (signerEmail == null || signerEmail.isBlank()) throw new IllegalArgumentException("signerEmail is required");
                if (signerDocumentation == null || signerDocumentation.isBlank()) {
                    throw new IllegalArgumentException("signerDocumentation is required");
                }
                if (signerBirthday == null || signerBirthday.isBlank()) {
                    throw new IllegalArgumentException("signerBirthday is required");
                }
                if (apiEmail == null || apiEmail.isBlank()) throw new IllegalArgumentException("apiEmail is required");
                if (adminEmail == null || adminEmail.isBlank()) throw new IllegalArgumentException("adminEmail is required");
                return new CreateParams(this);
            }
        }
    }
}
