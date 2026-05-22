Prepare uma nova release do SDK Java:

1. Leia o CHANGELOG.md atual e os commits desde a última release:
   ```bash
   git log $(git describe --tags --abbrev=0 2>/dev/null || echo main)..HEAD --oneline
   ```
2. Determine o próximo número de versão (semver) baseado nas mudanças
3. Atualize o arquivo `REVISION` com apenas o novo número de versão
4. Atualize o `CHANGELOG.md` com as mudanças agrupadas (Added, Changed, Fixed, Removed)
5. Mostre o diff e aguarde confirmação antes de commitar
6. Após confirmação:
   - Commit `REVISION` + `CHANGELOG` no `main`, push
   - `git checkout -b release/X.Y.Z && git push -u origin release/X.Y.Z`
   - Push na branch `release/*` dispara CI (Java 11/17/21) → publish Maven Central + tag `vX.Y.Z`
   - Secrets necessários: `MAVEN_USERNAME`, `MAVEN_PASSWORD`, `SIGNING_KEY`, `SIGNING_PASSWORD`
   - Após CI verde: merge `release/X.Y.Z` no `main`

## Checklist pré-release

- [ ] `./gradlew build` verde (compile + test + lint)
- [ ] `REVISION` atualizado
- [ ] `CHANGELOG.md` atualizado
- [ ] Nenhum item de alta prioridade aberto em `docs/TODO.md`
- [ ] Secrets de publicação configurados no repositório
