# Simples Finanças

App Android de finanças pessoais que trata receitas e despesas como **tarefas mensais**.
Concluir uma receita soma ao orçamento do mês; concluir uma despesa subtrai dele.

Kotlin + Jetpack Compose, tudo no aparelho: nenhum servidor.

## Rodando

Requer o Android SDK (`~/Android/Sdk`) e **JDK 21** — o Android Gradle Plugin não roda em
JDKs mais novos.

```bash
export JAVA_HOME=~/.jdks/jdk-21.0.12.1+1

./gradlew assembleDebug          # gera o APK de debug
./gradlew installDebug           # instala no emulador/aparelho conectado
./gradlew :app:testDebugUnitTest # testes JVM
./gradlew :app:lintDebug         # lint
```

Abrindo no Android Studio, basta o Sync — o `settings.gradle.kts` resolve o JDK do toolchain
sozinho.

## Como funciona

O usuário cadastra **modelos** de tarefa ("Aluguel, R$ 1.500, todo dia 10"). As tarefas de um
mês não são gravadas: são derivadas do modelo na hora de mostrar o mês. Por isso dá para
navegar para qualquer mês, passado ou futuro, sem gerar nada antes. O que fica gravado por
ocorrência é só o que o usuário mexeu — se concluiu e um eventual ajuste de valor daquele mês.

Detalhes de arquitetura, invariantes e convenções estão no [CLAUDE.md](./CLAUDE.md).
