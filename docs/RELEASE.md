# Processo de Release — Clicksign Java SDK

## Pré-requisitos (uma vez)

### 1. Conta no Sonatype Central Portal

Registrar em [central.sonatype.com](https://central.sonatype.com) e solicitar acesso ao `groupId` `com.clicksign`.

Após aprovação, gerar um token em **Account → Access Tokens** e adicionar nos secrets do repositório:
- `OSSRH_USERNAME` — login (e-mail ou username)
- `OSSRH_PASSWORD` — token gerado

### 2. Chave GPG

```bash
# Gerar chave (se não tiver)
gpg --gen-key

# Listar para obter KEY_ID
gpg --list-secret-keys --keyid-format LONG

# Exportar chave armored (vai para o secret SIGNING_KEY)
gpg --armor --export-secret-keys <KEY_ID>

# Publicar chave pública no keyserver (obrigatório para Maven Central)
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

Adicionar nos secrets do repositório:
- `SIGNING_KEY` — saída do `--armor --export-secret-keys` (bloco `-----BEGIN PGP...`)
- `SIGNING_PASSWORD` — passphrase da chave

---

## Checklist de release

### 1. Atualizar versão

Editar `REVISION` com a nova versão (`X.Y.Z`, sem `v`):

```bash
echo "0.2.0" > REVISION
```

### 2. Atualizar CHANGELOG

Mover entradas de `[Unreleased]` para `[X.Y.Z] - YYYY-MM-DD`:

```markdown
## [0.2.0] - 2026-06-01

### Added
- ...
```

### 3. Commit e push em `main`

```bash
git add REVISION CHANGELOG.md
git commit -m "chore: release 0.2.0"
git push origin main
```

### 4. Criar e fazer push da branch de release

```bash
git checkout -b release/0.2.0
git push origin release/0.2.0
```

O push dispara o workflow `.github/workflows/release.yml`:
1. **validate** — confirma que `release/0.2.0` == `REVISION`
2. **test** — Java 11, 17 e 21
3. **lint** — Checkstyle
4. **publish** — publica no Sonatype staging e cria tag `v0.2.0`

### 5. Promover no Sonatype (se não usar auto-release)

Acesse [s01.oss.sonatype.org](https://s01.oss.sonatype.org), vá em **Staging Repositories**, selecione o repositório da Clicksign e clique em **Close** → **Release**.

O artefato fica disponível no Maven Central em ~15–30 minutos.

### 6. Verificar publicação

```bash
# Verificar tag criada
git fetch --tags
git tag | grep v0.2.0

# Verificar no Maven Central (pode demorar até 30 min)
curl -s "https://search.maven.org/solrsearch/select?q=g:com.clicksign+a:clicksign-java-sdk&rows=1" | python3 -m json.tool
```

---

## Estrutura de artefatos publicados

| Artefato | Descrição |
|----------|-----------|
| `clicksign-java-sdk-X.Y.Z.jar` | Bytecode principal |
| `clicksign-java-sdk-X.Y.Z-sources.jar` | Código-fonte |
| `clicksign-java-sdk-X.Y.Z-javadoc.jar` | Javadoc |
| `clicksign-java-sdk-X.Y.Z.pom` | POM com metadados |
| `*.asc` | Assinaturas GPG de cada artefato |

---

## Secrets necessários

| Secret | Onde obter |
|--------|-----------|
| `OSSRH_USERNAME` | Central Portal → Account → Access Tokens |
| `OSSRH_PASSWORD` | Central Portal → Account → Access Tokens |
| `SIGNING_KEY` | `gpg --armor --export-secret-keys <KEY_ID>` |
| `SIGNING_PASSWORD` | Passphrase da chave GPG |

Configurar em: **GitHub → Settings → Secrets and variables → Actions**.
