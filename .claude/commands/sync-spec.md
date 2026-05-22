# sync-spec

Compare os endpoints documentados na API Clicksign com os resources implementados no SDK.

## Passo 1 — Buscar endpoints da API

Leia `https://developers.clicksign.com/llms.txt` para obter lista completa de endpoints.
Para cada resource, leia a página de campos e regras de negócio.

## Passo 2 — Mapear resources do SDK

Liste todos os `Service` implementados em:
- `src/main/java/com/clicksign/resources/notarial/`
- `src/main/java/com/clicksign/resources/`

Para cada Service, liste os métodos públicos (list/retrieve/create/update/delete + especiais).

## Passo 3 — Comparar e reportar gaps

Para cada resource, reportar:

| Resource | API tem | SDK tem | Status |
|----------|---------|---------|--------|
| Envelope | list/retrieve/create/update/delete/activate | ... | ✅ / ❌ |

Reportar também:
- Campos faltando na entidade ou nos params (comparar com campos da API)
- Métodos no SDK que a API NÃO documenta (candidatos a remoção)
- Tipos errados (ex: objeto mapeado como String)

## Passo 4 — Atualizar docs/TODO.md

Adicionar gaps encontrados em "Pendente" com prioridade sugerida.
