---
marp: true
paginate: true
size: 16:9
lang: pt-BR
title: Simples Finanças
style: |
  @import url('https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,500;9..144,700&family=Manrope:wght@400;500;600;700;800&display=swap');

  /* A paleta é a do próprio app (ui/theme/Color.kt), valor a valor. */
  :root {
    --ink: #173a40;
    --ink-soft: #416166;
    --lagoon: #4fb8b2;
    --lagoon-deep: #328f97;
    --palm: #2f6a4a;
    --coral: #bd4f3c;
    --foam: #f3faf5;
    --line: rgba(23, 58, 64, 0.14);
  }

  section {
    font-family: 'Manrope', system-ui, sans-serif;
    color: var(--ink);
    padding: 64px 80px;
    font-size: 26px;
    line-height: 1.5;
    background:
      radial-gradient(900px 520px at -8% -12%, rgba(79, 184, 178, 0.30), transparent 60%),
      radial-gradient(860px 520px at 112% -14%, rgba(47, 106, 74, 0.18), transparent 62%),
      linear-gradient(180deg, #eef6f0 0%, var(--foam) 44%, #e7f3ec 100%);
  }

  section::after {
    font-family: 'Manrope', sans-serif;
    font-size: 16px;
    font-weight: 700;
    color: var(--ink-soft);
    opacity: 0.6;
  }

  h1, h2 {
    font-family: 'Fraunces', Georgia, serif;
    color: var(--ink);
    letter-spacing: -0.01em;
    line-height: 1.1;
    margin: 0;
  }
  h1 { font-size: 64px; font-weight: 700; }
  h2 { font-size: 44px; font-weight: 700; margin-bottom: 28px; }

  p { margin: 0 0 18px; }
  strong { color: var(--ink); font-weight: 700; }

  .kicker {
    font-size: 15px;
    font-weight: 800;
    letter-spacing: 0.16em;
    text-transform: uppercase;
    color: rgba(47, 106, 74, 0.9);
    margin-bottom: 14px;
  }

  .soft { color: var(--ink-soft); }
  .palm { color: var(--palm); }
  .coral { color: var(--coral); }

  /* ---------- Capa ---------- */
  section.capa {
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  section.capa h1 { font-size: 96px; }
  section.capa .sub {
    font-size: 30px;
    color: var(--ink-soft);
    max-width: 780px;
    margin-top: 22px;
  }
  section.capa .rodape {
    margin-top: 64px;
    font-size: 18px;
    color: var(--ink-soft);
  }

  /* ---------- O problema: a lista de contas riscada ---------- */
  .cols { display: grid; grid-template-columns: 1fr 1fr; gap: 64px; align-items: center; }
  section.tela { padding: 40px 80px 40px 96px; }
  .cols-telas { display: grid; grid-template-columns: auto 1fr; gap: 72px; align-items: center; height: 100%; }

  .ilha {
    border: 1px solid var(--line);
    border-radius: 24px;
    background: linear-gradient(165deg, rgba(255,255,255,0.92), rgba(255,255,255,0.74));
    box-shadow: 0 1px 0 rgba(255,255,255,0.82) inset, 0 22px 44px rgba(30,90,72,0.10), 0 6px 18px rgba(23,58,64,0.08);
    padding: 26px 30px;
  }

  .conta {
    display: grid;
    grid-template-columns: 30px 44px 1fr auto;
    align-items: center;
    gap: 14px;
    padding: 9px 0;
    font-size: 22px;
    border-bottom: 1px solid var(--line);
  }
  .conta:last-child { border-bottom: 0; }
  .conta .check {
    width: 24px; height: 24px; border-radius: 50%;
    border: 1.5px solid rgba(47,106,74,0.3);
    display: grid; place-items: center;
    color: white; font-size: 14px; font-weight: 800;
  }
  .conta.feita.receita .check { background: var(--palm); border-color: var(--palm); }
  .conta.feita.despesa .check { background: var(--coral); border-color: var(--coral); }
  .conta .dia {
    font-size: 14px; font-weight: 800; text-align: center;
    background: rgba(255,255,255,0.8); border-radius: 8px; padding: 3px 0;
    color: var(--ink-soft);
  }
  .conta.vencida .dia { background: rgba(189,79,60,0.12); color: var(--coral); }
  .conta .valor { font-weight: 700; font-variant-numeric: tabular-nums; color: var(--ink-soft); }
  .conta.feita .nome { text-decoration: line-through; opacity: 0.55; }
  .conta.feita.receita .valor { color: var(--palm); }
  .conta.feita.despesa .valor { color: var(--coral); }

  .pergunta {
    font-family: 'Fraunces', serif;
    font-size: 40px;
    font-weight: 700;
    line-height: 1.2;
  }

  /* ---------- A ideia ---------- */
  .equacao {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 28px;
    margin-top: 12px;
  }
  .equacao .ilha h3 {
    font-family: 'Fraunces', serif;
    font-size: 30px;
    margin: 0 0 10px;
  }
  .equacao .ilha p { font-size: 22px; color: var(--ink-soft); margin: 0; }
  .nota {
    margin-top: 34px;
    font-size: 22px;
    color: var(--ink-soft);
    border-left: 3px solid var(--lagoon);
    padding-left: 18px;
  }

  /* ---------- Telas ---------- */
  .celular {
    height: 620px;
    aspect-ratio: 1080 / 2400;
    border-radius: 34px;
    padding: 9px;
    box-sizing: border-box;
    background: #0f1a1e;
    box-shadow: 0 30px 60px rgba(23,58,64,0.28), 0 8px 18px rgba(23,58,64,0.18);
  }
  .celular img { width: 100%; height: 100%; object-fit: cover; object-position: top; border-radius: 26px; display: block; }

  .pontos { list-style: none; padding: 0; margin: 0; }
  .pontos li {
    font-size: 21px;
    line-height: 1.45;
    padding: 10px 0 10px 22px;
    border-bottom: 1px solid var(--line);
    position: relative;
  }
  .pontos li:last-child { border-bottom: 0; }
  .pontos li::before {
    content: '';
    position: absolute; left: 0; top: 21px;
    width: 8px; height: 8px; border-radius: 50%;
    background: var(--lagoon-deep);
  }
  .pontos b { color: var(--ink); }
  .pontos b.coral { color: var(--coral); }
  section.tela h2 { font-size: 40px; margin-bottom: 16px; }


  /* ---------- Telas desenhadas com os tokens do app ---------- */
  .celular { background: #0f1a1e; }
  .app {
    width: 100%; height: 100%; border-radius: 26px; overflow: hidden;
    background:
      radial-gradient(260px 180px at -10% -8%, rgba(79,184,178,0.36), transparent 60%),
      radial-gradient(240px 180px at 112% -10%, rgba(47,106,74,0.2), transparent 62%),
      linear-gradient(180deg, #eef6f0 0%, var(--foam) 44%, #e7f3ec 100%);
    padding: 26px 14px 14px; box-sizing: border-box;
    font-size: 11px; line-height: 1.35; color: var(--ink);
    display: flex; flex-direction: column; gap: 9px;
  }
  .app .k { font-size: 7px; font-weight: 800; letter-spacing: 0.16em; text-transform: uppercase; color: rgba(47,106,74,0.9); }
  .app .ttl { font-family: 'Fraunces', serif; font-weight: 700; font-size: 19px; line-height: 1.1; }
  .app .nav { display: flex; align-items: center; gap: 6px; font-family: 'Fraunces', serif; font-weight: 700; font-size: 12px; }
  .app .nav i { font-style: normal; width: 20px; height: 20px; border-radius: 50%; border: 1px solid var(--line); background: rgba(255,255,255,0.8); display: grid; place-items: center; color: var(--ink-soft); font-size: 11px; }
  .app .card { border: 1px solid var(--line); border-radius: 14px; background: linear-gradient(165deg, rgba(255,255,255,0.92), rgba(255,255,255,0.74)); box-shadow: 0 8px 18px rgba(30,90,72,0.08); padding: 11px 12px; }
  .app .big { font-family: 'Fraunces', serif; font-weight: 700; font-size: 25px; line-height: 1.1; margin: 3px 0 2px; }
  .app .hint { font-size: 8px; color: var(--ink-soft); }
  .app .bar { height: 5px; border-radius: 9px; background: rgba(47,106,74,0.18); margin: 5px 0; overflow: hidden; }
  .app .bar b { display: block; height: 100%; width: 43%; background: linear-gradient(90deg, var(--lagoon), var(--palm)); border-radius: 9px; }
  .app .row2 { display: flex; justify-content: space-between; font-size: 8.5px; }
  .app .h { display: flex; justify-content: space-between; align-items: baseline; border-bottom: 1px solid var(--line); padding-bottom: 6px; margin-bottom: 3px; }
  .app .h span:first-child { font-family: 'Fraunces', serif; font-weight: 700; font-size: 14px; }
  .app .h span:last-child { font-family: 'Fraunces', serif; font-weight: 700; font-size: 12px; }
  .app .t { display: grid; grid-template-columns: 15px 19px 1fr auto; gap: 6px; align-items: center; padding: 4.5px 0; font-size: 10px; }
  .app .t .c { width: 13px; height: 13px; border-radius: 50%; border: 1px solid rgba(47,106,74,0.3); background: rgba(255,255,255,0.9); color: white; font-size: 8px; display: grid; place-items: center; font-weight: 800; }
  .app .t .d { font-size: 7.5px; font-weight: 800; text-align: center; background: rgba(255,255,255,0.8); border-radius: 4px; padding: 1.5px 0; color: var(--ink-soft); }
  .app .t .v { font-weight: 700; font-size: 9.5px; color: var(--ink-soft); }
  .app .t.ok .n { text-decoration: line-through; opacity: 0.55; }
  .app .t.ok.r .c { background: var(--palm); border-color: var(--palm); }
  .app .t.ok.r .v { color: var(--palm); }
  .app .t.ok.x .c { background: var(--coral); border-color: var(--coral); }
  .app .t.ok.x .v { color: var(--coral); }
  .app .t.late .d { background: rgba(189,79,60,0.12); color: var(--coral); }
  .app .sub { font-size: 7.5px; color: var(--ink-soft); display: block; }
  .app.dim { position: relative; }
  .app.dim::before { content: ''; position: absolute; inset: 0; background: rgba(23,58,64,0.38); }
  .app .dlg { position: relative; margin-top: auto; margin-bottom: auto; }
  .app .lab { font-size: 7px; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: var(--ink-soft); margin: 8px 0 4px; }
  .app .seg { display: flex; gap: 4px; }
  .app .seg span { flex: 1; text-align: center; border: 1px solid var(--line); border-radius: 8px; padding: 5px 0; font-weight: 700; font-size: 9px; color: var(--ink-soft); background: rgba(255,255,255,0.8); }
  .app .seg span.on { border-color: var(--lagoon-deep); background: rgba(47,106,74,0.12); color: var(--ink); }
  .app .inp { border: 1px solid var(--line); border-radius: 8px; padding: 6px 8px; background: rgba(255,255,255,0.9); font-size: 10.5px; }
  .app .inp.f { border-color: var(--lagoon-deep); }
  .app .btns { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; font-size: 9.5px; font-weight: 700; color: var(--ink-soft); }
  .app .btns .save { background: var(--ink); color: var(--foam); border-radius: 8px; padding: 6px 13px; }

  /* ---------- Fechamento ---------- */
  section.fim {
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  section.fim .pergunta { font-size: 56px; max-width: 980px; }
---

<!-- _class: capa -->
<!-- _paginate: false -->

<div class="kicker">Finanças como tarefas</div>

# Simples Finanças

<div class="sub">Um app para lembrar o que você já recebeu e o que ainda falta pagar neste mês.</div>

<div class="rodape">Davi Alexandre · App Android em Kotlin + Jetpack Compose</div>

---

<div class="kicker">O problema</div>

<div class="cols">
<div>

## Todo mês, a mesma lista

O salário cai e, junto com ele, chega a fila de sempre: aluguel, cartão, luz, internet.

As contas são quase as mesmas todo mês — o difícil é **lembrar quais já foram pagas** neste mês.

<p class="soft">Planilha dá trabalho demais. App de controle financeiro pede categoria, gráfico e extrato. Para essa pergunta, os dois sobram.</p>

</div>
<div>

<div class="ilha">
  <div class="conta feita receita"><span class="check">✓</span><span class="dia">05</span><span class="nome">Salário</span><span class="valor">+ R$ 4.800</span></div>
  <div class="conta feita despesa"><span class="check">✓</span><span class="dia">10</span><span class="nome">Aluguel</span><span class="valor">− R$ 1.500</span></div>
  <div class="conta vencida despesa"><span class="check"></span><span class="dia">12</span><span class="nome">Energia</span><span class="valor">− R$ 180</span></div>
  <div class="conta despesa"><span class="check"></span><span class="dia">14</span><span class="nome">Cartão de crédito</span><span class="valor">− R$ 1.200</span></div>
  <div class="conta despesa"><span class="check"></span><span class="dia">15</span><span class="nome">Internet</span><span class="valor">− R$ 120</span></div>
</div>

<p class="pergunta" style="margin-top: 40px">“A luz desse mês eu já paguei?”</p>

</div>
</div>

---

<div class="kicker">A ideia</div>

## Cada receita e cada despesa vira uma tarefa

<div class="equacao">
  <div class="ilha">
    <h3 class="palm">Receita concluída</h3>
    <p>O dinheiro entrou. Marcou, o valor <strong class="palm">soma</strong> ao que está disponível no mês.</p>
  </div>
  <div class="ilha">
    <h3 class="coral">Despesa concluída</h3>
    <p>A conta foi paga. Marcou, o valor <strong class="coral">sai</strong> do que está disponível no mês.</p>
  </div>
</div>

<div class="nota">Você cadastra a conta <strong>uma vez</strong> — “Aluguel, R$ 1.500, todo dia 10” — e ela aparece em todos os meses como uma tarefa nova, esperando ser marcada.</div>

---

<!-- _class: tela -->

<div class="cols-telas">
<div class="celular"><div class="app">
<div><div class="k">Finanças como tarefas</div><div class="ttl">Simples Finanças</div></div>
<div class="nav"><i>‹</i> Setembro de 2026 <i>›</i></div>
<div class="card">
<div class="k">Orçamento de setembro de 2026</div>
<div class="big">R$ 3.790,00</div>
<div class="hint">disponível agora — o que já entrou menos o que já foi pago</div>
<div class="row2" style="margin-top:7px"><b>3 de 7 tarefas</b><span class="hint">43%</span></div>
<div class="bar"><b></b></div>
<div class="row2"><span class="hint">Fim do mês previsto:</span><b>R$ 1.510,00</b></div>
</div>
<div class="card">
<div class="h"><span>Receitas</span><span class="palm">R$ 5.400,00</span></div>
<div class="t ok r"><span class="c">✓</span><span class="d">05</span><span class="n">Salário<span class="sub">↻ todo mês</span></span><span class="v">+ R$ 4.800,00</span></div>
<div class="t r"><span class="c"></span><span class="d">20</span><span class="n">Freela<span class="sub">↻ todo mês</span></span><span class="v">+ R$ 600,00</span></div>
</div>
<div class="card">
<div class="h"><span>Despesas</span><span class="coral">R$ 3.890,00</span></div>
<div class="t ok x"><span class="c">✓</span><span class="d">03</span><span class="n">Academia</span><span class="v">− R$ 110,00</span></div>
<div class="t ok x"><span class="c">✓</span><span class="d">08</span><span class="n">Mercado</span><span class="v">− R$ 900,00</span></div>
<div class="t late x"><span class="c"></span><span class="d">10</span><span class="n">Aluguel</span><span class="v">− R$ 1.500,00</span></div>
<div class="t late x"><span class="c"></span><span class="d">12</span><span class="n">Energia</span><span class="v">− R$ 180,00</span></div>
<div class="t x"><span class="c"></span><span class="d">14</span><span class="n">Cartão de crédito</span><span class="v">− R$ 1.200,00</span></div>
</div>
</div></div>
<div>

<div class="kicker">Tela 1 · O mês</div>

## O que já foi feito, o que falta

<ul class="pontos">
  <li><b>Disponível agora:</b> o que já entrou menos o que já foi pago — o dinheiro que realmente sobra hoje.</li>
  <li><b>Fim do mês previsto:</b> quanto vai sobrar se tudo o que falta acontecer.</li>
  <li>Receitas e despesas em listas separadas; um toque no círculo marca como <b>feita</b>.</li>
  <li>Conta vencida e ainda pendente fica com o dia em <b class="coral">vermelho</b>.</li>
  <li>As setas no topo passam para o mês seguinte — as contas fixas já estão lá, todas pendentes.</li>
</ul>

</div>
</div>

---

<!-- _class: tela -->

<div class="cols-telas">
<div class="celular"><div class="app dim">
<div class="card dlg">
<div class="k">Editar tarefa</div>
<div class="ttl" style="font-size:17px;margin-top:3px">Energia</div>
<div class="lab">Tipo</div>
<div class="seg"><span>Receita</span><span class="on">Despesa</span></div>
<div class="lab">Descrição</div>
<div class="inp">Energia</div>
<div style="display:grid;grid-template-columns:1fr 54px;gap:6px">
<div><div class="lab">Valor</div><div class="inp f"><span class="soft">R$</span> 212,40</div></div>
<div><div class="lab">Dia</div><div class="inp">12</div></div>
</div>
<div class="lab">Repetição</div>
<div class="seg"><span class="on">Todo mês</span><span>Só em Setembro</span></div>
<div class="lab">O novo valor vale para</div>
<div class="seg"><span>Todos os meses</span><span class="on">Só setembro</span></div>
<div class="btns"><span class="coral">🗑 Remover</span><span>Cancelar &nbsp; <span class="save">Salvar</span></span></div>
</div>
</div></div>
<div>

<div class="kicker">Tela 2 · Cadastrar uma conta</div>

## Cadastra uma vez, vale todo mês

<ul class="pontos">
  <li><b>Receita ou despesa</b>, descrição, valor e o dia do vencimento.</li>
  <li><b>Todo mês</b> para contas fixas; <b>Só neste mês</b> para um gasto avulso.</li>
  <li>A luz veio diferente? O novo valor pode valer para <b>todos os meses</b> ou <b>só para este</b>.</li>
  <li>Parou de pagar a academia? <b>Encerrar depois deste mês</b> tira a conta dos meses seguintes sem apagar o que já foi pago.</li>
</ul>

</div>
</div>

---

<!-- _class: fim -->
<!-- _paginate: false -->

<div class="kicker">Simples Finanças</div>

<p class="pergunta">Recebeu, pagou, marcou. No mês seguinte, a lista recomeça sozinha.</p>

<p class="soft" style="margin-top: 36px; font-size: 22px">Obrigado!</p>
