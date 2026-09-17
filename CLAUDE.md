# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## O produto

App Android de finanças pessoais que trata receitas e despesas como **tarefas mensais**.
Concluir uma tarefa de receita soma ao orçamento do mês; concluir uma despesa subtrai. A UI é
toda em **pt-BR** — textos, rótulos, formatação de moeda e de datas.

Kotlin + Jetpack Compose, um módulo (`:app`), sem backend: tudo mora no aparelho.

## Comandos

O AGP não roda em JDK 26 (o padrão desta máquina). Exporte o JDK 21 antes:

```bash
export JAVA_HOME=~/.jdks/jdk-21.0.12.1+1

./gradlew assembleDebug        # compila o APK de debug
./gradlew :app:testDebugUnitTest  # testes JVM (paridade com a versão web)
./gradlew :app:lintDebug       # lint do Android
./gradlew installDebug         # instala no emulador/aparelho conectado
```

`local.properties` (com `sdk.dir`) não é versionado; o SDK está em `~/Android/Sdk`.

**Não há TDD aqui** — Davi optou explicitamente por não trabalhar test-first. O que existe é
`app/src/test/kotlin/.../ParityTest.kt`, escrito depois do port para travar as regras que não
podiam mudar (parsing de dinheiro, `clampDay`, derivação de ocorrências, formato do JSON).
Para experimentar lógica de domínio rapidamente, escreva um teste JUnit novo ali: as camadas `domain/` e `lib/` são Kotlin puro e rodam sem emulador.

## Arquitetura

### Ocorrências são derivadas, não gravadas

O ponto central do modelo: o usuário cadastra **modelos** (`TaskTemplate`) — "Aluguel,
R$ 1.500, todo dia 10". As tarefas de um mês (`Occurrence`) **não existem em disco**; são
calculadas sob demanda por `buildMonthView(state, month)` em `domain/Ledger.kt`, que é o
**único caminho de leitura da tela**. Consequência prática: navegar para qualquer mês, passado
ou futuro, funciona sem migração nem geração prévia de dados.

O que é gravado por ocorrência é só o que o usuário mexeu, em `state.occurrences` com chave
`${templateId}:${YYYY-MM}`: se está concluída e um eventual **ajuste pontual de valor**
("a luz desse mês veio R$ 212,40"). Ausência de entrada = tarefa pendente com o valor do modelo.

Encerrar um recorrente usa `endMonth` (`endTemplateAfter`), preservando o histórico já
concluído; `deleteTemplate` apaga o modelo **e** todas as ocorrências dele.

### Camadas

| Caminho (`com.simplesfinancas.app.…`) | Papel |
| --- | --- |
| `domain/Types.kt` | Vocabulário: `TaskTemplate`, `Occurrence`, `MonthSummary`, `MonthView` |
| `domain/Ledger.kt` | Derivação de ocorrências + cálculo do orçamento. Puro, sem Compose |
| `store/FinanceStore.kt` | Estado financeiro (`StateFlow`) + persistência + ações mutadoras |
| `AppContainer.kt` | Composition root: monta armazenamento e stores uma vez só |
| `lib/Month.kt`, `lib/Money.kt` | `MonthKey` e centavos (ver invariantes abaixo) |
| `lib/KeyValueStore.kt`, `lib/Json.kt` | O "localStorage" do app e o `Json` compartilhado |
| `ui/*` | Compose sem estado de negócio; chamam as ações das stores direto |
| `ui/AppRoot.kt` | `Dashboard` e a composição da tela |
| `ui/theme/*` | A identidade visual (paleta, fontes, `islandShell`, fundo) |

### O estado mora numa chave só

`FinanceStore` grava tudo em `simples-financas:v1`, a mesma chave da versão web. As gravações
passam por um `Channel` serial, para que a ordem no disco seja a ordem das ações.

### Invariantes que o código assume

- **Dinheiro é `Long` em centavos**, sempre inteiro. Nenhum float de reais circula. Entrada do
  usuário passa por `parseAmountToCents` (entende `1.800`, `1.234,56`, `99.9`); saída sempre
  por `formatBRL`.
- **`MonthKey` é a string `"YYYY-MM"`** — comparação lexicográfica (`<`, `>`) é comparação
  cronológica, e o código depende disso em `occursIn` e nos limites `startMonth`/`endMonth`.
- **Dia da ocorrência passa por `clampDay`**: "todo dia 31" vira 28/29 em fevereiro em vez de
  sumir do mês.
- **O JSON gravado é o mesmo da versão web**: `Recurrence` é polimórfico com discriminador
  `type` (`"monthly"`/`"once"`), `TaskKind` serializa como `"income"`/`"expense"` e
  `AppJson` usa `explicitNulls = false` para que um ajuste ausente seja campo ausente, não
  `null` (era um `delete` no original).

### Estado e ciclo de vida

- As stores são **singletons de aplicação** (`AppContainer`), não `ViewModel`s: expõem
  `StateFlow` e a UI lê com `collectAsStateWithLifecycle()`.
- Estado de tela (mês visitado, diálogo aberto, confirmações) vive em `remember`/
  `rememberSaveable` dentro dos composables.
- Qualquer coisa derivada de "hoje" (ex.: marcar tarefa vencida) só aparece depois da
  montagem — ver o `LaunchedEffect` de `today` em `ui/AppRoot.kt`. Não chame `todayIso()`
  direto no corpo do composable.

## Estilo e convenções

- **Tabs** para indentar e **aspas duplas**, como no projeto original.
- `ui/theme/` carrega a identidade visual da casa (paleta lagoon/palm/coral, Fraunces para
  display, Manrope para texto), portada valor a valor do `styles.css` da versão web. A paleta
  viaja por `LocalIslandColors` (Material 3 não tem slot para `ink`/`lagoon`/`palm`/`coral`);
  leia com `islandColors`. Peças prontas: `Modifier.islandShell()` (cartão de vidro),
  `Modifier.chipSurface()`, `IslandKicker` (rótulo maiúsculo), `Modifier.pageWrap()`,
  `Modifier.riseIn()`, `AppBackground`. **Reuse esses tokens** em vez de introduzir cores novas.
- Receita é verde (`palm`) e despesa é coral em toda a UI — inclusive nos sinais `+`/`−`.
- Todo texto visível vem de `res/values/strings.xml`.
- Tema claro e escuro seguem o sistema; ambas as paletas existem em `ui/theme/Color.kt`.
