# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## O produto

App de finanças pessoais que trata receitas e despesas como **tarefas mensais**. Concluir
uma tarefa de receita soma ao orçamento do mês; concluir uma despesa subtrai. A UI é toda
em **pt-BR** — textos, rótulos, formatação de moeda e de datas.

## Comandos

```bash
pnpm dev              # vite dev na porta 3000
pnpm build            # build de produção (client + server)
pnpm check            # biome: lint + format + organize imports (use --write para corrigir)
pnpm exec tsc --noEmit  # checagem de tipos (não há script para isso)
pnpm generate-routes  # regenera src/routeTree.gen.ts (o dev server já faz isso sozinho)
```

**Não há test runner** — Davi optou explicitamente por não trabalhar test-first aqui.
Para validar lógica de domínio rapidamente, escreva um script `.ts` **na raiz do repo** e
rode com `pnpm exec jiti ./script.ts` (jiti resolve o alias `#/`; fora da raiz não resolve).
Como as portas de autenticação são interfaces, dá para montar o `AuthService` com fakes em
memória e rodar tudo sem browser; para exercitar os adaptadores, basta plantar um
`globalThis.window` com um `localStorage` de mentira antes do `await import()`.

Componentes shadcn se instalam com `pnpm dlx shadcn@latest add <componente>` (new-york,
base zinc, destino `#/components/ui`). Até agora nenhum foi instalado — a UI é manual.

## Arquitetura

### Ocorrências são derivadas, não gravadas

O ponto central do modelo: o usuário cadastra **modelos** (`TaskTemplate`) — "Aluguel,
R$ 1.500, todo dia 10". As tarefas de um mês (`Occurrence`) **não existem em disco**; são
calculadas sob demanda por `buildMonthView(state, month)` em `src/domain/ledger.ts`, que é
o **único caminho de leitura da tela**. Consequência prática: navegar para qualquer mês,
passado ou futuro, funciona sem migração nem geração prévia de dados.

O que é gravado por ocorrência é só o que o usuário mexeu, em `state.occurrences` com chave
`${templateId}:${YYYY-MM}`: se está concluída e um eventual **ajuste pontual de valor**
("a luz desse mês veio R$ 212,40"). Ausência de entrada = tarefa pendente com o valor do
modelo.

Encerrar um recorrente usa `endMonth` (`endTemplateAfter`), preservando o histórico já
concluído; `deleteTemplate` apaga o modelo **e** todas as ocorrências dele.

### Camadas

| Caminho | Papel |
| --- | --- |
| `src/domain/types.ts` | Vocabulário: `TaskTemplate`, `Occurrence`, `MonthSummary`, `MonthView` |
| `src/domain/ledger.ts` | Derivação de ocorrências + cálculo do orçamento. Puro, sem React |
| `src/store/finance-store.ts` | Persistência em localStorage + ações mutadoras |
| `src/domain/auth/*` | Regra de autenticação: `types.ts`, `ports.ts` (interfaces) e `AuthService` |
| `src/auth/*` | Adaptadores das portas (WebCrypto, localStorage) + `container.ts` |
| `src/store/auth-store.ts`, `src/store/session-bridge.ts` | Sessão na UI e ligação com a store financeira |
| `src/lib/month.ts`, `src/lib/money.ts` | `MonthKey` e centavos (ver invariantes abaixo) |
| `src/components/*` | UI sem estado de negócio; chamam as ações do store direto |
| `src/routes/index.tsx` | Porteiro (`loading`/`LockScreen`/`Dashboard`) e composição da tela |

### Autenticação é local e depende só de interfaces

Contas moram no dispositivo: o usuário cria nome + senha, o app fica trancado até ele
entrar. Não há servidor. O desenho é intencionalmente invertido — `AuthService`
(`src/domain/auth/auth-service.ts`) é TypeScript puro e recebe **portas** pelo
construtor (`src/domain/auth/ports.ts`): `PasswordHasher`, `AccountReader`,
`AccountWriter`, `SessionStore`, `Clock`, `IdGenerator`. Ele não sabe o que é PBKDF2 nem
`localStorage`.

As implementações vivem em `src/auth/` e são montadas **só** em `src/auth/container.ts`.
Trocar o hash ou mandar as contas para um backend começa e termina nesse arquivo — não
mexa nas classes concretas a partir do domínio nem da UI.

Consequências práticas:

- **A senha nunca é comparada em texto.** `WebCryptoPasswordHasher` usa PBKDF2-SHA256 com
  salt por conta, e o digest carrega os próprios parâmetros para continuar verificável se
  o custo subir depois.
- **Erros são códigos, não exceções.** `AuthResult<T>` devolve `{ ok: false, error }` com
  `code` e `message` pt-BR já pronta (`authError` em `src/domain/auth/types.ts`).
- **A trava não cifra os dados.** O estado financeiro fica em texto puro no localStorage,
  só separado por usuário — protege contra quem pega o aparelho, não contra o DevTools.
  Cifrar em repouso seria um novo adaptador, sem tocar no domínio.

### Cada usuário tem sua gaveta no localStorage

`finance-store` não guarda mais tudo numa chave só: a chave é
`simples-financas:v1:u:<userId>`, definida por `setStorageScope(userId | null)`. Com
escopo `null` (ninguém logado) a store fica vazia e **não grava nada**.

As duas stores se ignoram de propósito — `finance-store` não importa `auth-store`. Quem
liga uma na outra é `src/store/session-bridge.ts` (`useSessionBridge()` no
`routes/index.tsx`). Se precisar reagir a login/logout em outro lugar, estenda a ponte,
não crie um import cruzado.

A chave antiga `simples-financas:v1` (de antes das contas) é adotada pelo primeiro
usuário que abrir uma gaveta vazia e depois removida — `adoptLegacyState`.

### Invariantes que o código assume

- **Dinheiro é `number` em centavos**, sempre inteiro. Nenhum float de reais circula.
  Entrada do usuário passa por `parseAmountToCents` (entende `1.800`, `1.234,56`, `99.9`);
  saída sempre por `formatBRL`.
- **`MonthKey` é a string `"YYYY-MM"`** — comparação lexicográfica (`<`, `>`) é comparação
  cronológica, e o código depende disso em `occursIn` e nos limites `startMonth`/`endMonth`.
- **Dia da ocorrência passa por `clampDay`**: "todo dia 31" vira 28/29 em fevereiro em vez
  de sumir do mês.

### SSR e localStorage

O app renderiza no servidor (TanStack Start), onde `localStorage` não existe. O store é um
external store de módulo consumido por `useSyncExternalStore`, e a regra é:

- `getServerSnapshot` devolve o estado **vazio**; a hidratação do localStorage acontece
  dentro de `subscribe` (já no cliente), **nunca durante o render**.
- Qualquer coisa derivada de "hoje" (ex.: marcar tarefa vencida) só pode aparecer depois da
  montagem — ver o `useState`/`useEffect` de `today` em `src/routes/index.tsx`. Não chame
  `todayIso()` direto no corpo do componente.
- `auth-store` segue a mesma regra e vai além: seu `getServerSnapshot` devolve uma
  **constante de módulo congelada** (`LOADING`). Precisa ser a mesma referência a cada
  chamada, senão `useSyncExternalStore` entra em laço. O servidor renderiza o mesmo
  esqueleto (`LoadingShell`) que o cliente mostra antes de restaurar a sessão — é isso que
  faz a hidratação bater.

## Estilo e convenções

- **Biome** com **tabs** e **aspas duplas**. `src/styles.css` e `src/routeTree.gen.ts` estão
  fora da checagem — o `routeTree.gen.ts` é gerado, não edite.
- Imports internos usam o alias **`#/`** (mapeado em `package.json#imports` e no tsconfig).
- `src/styles.css` carrega a identidade visual da casa (paleta lagoon/palm/coral, Fraunces
  para display, Manrope para texto) e expõe tudo como utilitário Tailwind: `text-ink`,
  `text-ink-soft`, `text-palm` (receita), `text-coral` (despesa), `border-line`, `bg-foam`,
  `font-display`. Classes prontas: `.island-shell` (cartão de vidro), `.island-kicker`
  (rótulo maiúsculo), `.page-wrap`, `.rise-in`. **Reuse esses tokens** em vez de introduzir
  cores novas do Tailwind.
- Receita é verde (`palm`) e despesa é coral em toda a UI — inclusive nos sinais `+`/`−`.
