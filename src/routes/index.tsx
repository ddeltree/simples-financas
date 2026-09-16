import { createFileRoute } from "@tanstack/react-router";
import { Sparkles, Wallet } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { AccountMenu } from "#/components/auth/account-menu";
import { LockScreen } from "#/components/auth/lock-screen";
import { BudgetSummary } from "#/components/budget-summary";
import { MonthNav } from "#/components/month-nav";
import { TaskDialog, type TaskEditor } from "#/components/task-dialog";
import { TaskList } from "#/components/task-list";
import type { AccountSummary } from "#/domain/auth/types";
import { buildMonthView } from "#/domain/ledger";
import type { Occurrence, TaskKind } from "#/domain/types";
import { currentMonthKey, todayIso } from "#/lib/month";
import { useAuthState } from "#/store/auth-store";
import {
	resetAll,
	seedExamples,
	setOccurrenceDone,
	useFinanceState,
} from "#/store/finance-store";
import { useSessionBridge } from "#/store/session-bridge";

export const Route = createFileRoute("/")({ component: Home });

function Home() {
	useSessionBridge();
	const auth = useAuthState();

	// No servidor ninguém está logado ainda: renderiza o mesmo esqueleto que o cliente
	// mostra antes de restaurar a sessão, para a hidratação bater.
	if (auth.status === "loading") return <LoadingShell />;
	if (auth.user === null) return <LockScreen />;

	return <Dashboard user={auth.user} />;
}

function LoadingShell() {
	return (
		<main className="page-wrap flex min-h-dvh items-center justify-center py-10">
			<p className="text-sm text-ink-soft">Abrindo suas finanças…</p>
		</main>
	);
}

function EmptyState({ onCreate }: { onCreate: (kind: TaskKind) => void }) {
	return (
		<section className="island-shell rounded-3xl p-8 text-center sm:p-12">
			<div className="mx-auto grid size-14 place-items-center rounded-2xl bg-palm-soft text-palm">
				<Wallet className="size-7" />
			</div>
			<h2 className="mt-5 font-display text-2xl font-semibold text-ink">
				Seu mês ainda está em branco
			</h2>
			<p className="mx-auto mt-2 max-w-md text-ink-soft">
				Cadastre o que entra e o que sai como tarefas. Marcar uma receita como
				concluída aumenta o orçamento do mês; marcar uma despesa subtrai dele.
			</p>
			<div className="mt-6 flex flex-wrap items-center justify-center gap-3">
				<button
					type="button"
					onClick={() => onCreate("expense")}
					className="rounded-xl bg-ink px-5 py-2.5 text-sm font-semibold text-[var(--foam)] hover:opacity-90"
				>
					Cadastrar primeira despesa
				</button>
				<button
					type="button"
					onClick={() => onCreate("income")}
					className="rounded-xl border border-line bg-[var(--chip-bg)] px-5 py-2.5 text-sm font-semibold text-ink hover:border-lagoon-deep"
				>
					Cadastrar uma receita
				</button>
				<button
					type="button"
					onClick={seedExamples}
					className="inline-flex items-center gap-1.5 px-2 py-2 text-sm font-semibold text-ink-soft hover:text-ink"
				>
					<Sparkles className="size-4" /> Começar com exemplos
				</button>
			</div>
		</section>
	);
}

function Dashboard({ user }: { user: AccountSummary }) {
	const state = useFinanceState();
	const [month, setMonth] = useState(currentMonthKey);
	const [editor, setEditor] = useState<TaskEditor | null>(null);
	const [confirmingReset, setConfirmingReset] = useState(false);

	// Só depois da hidratação — no servidor não existe "hoje" do usuário.
	const [today, setToday] = useState<string | null>(null);
	useEffect(() => setToday(todayIso()), []);

	const view = useMemo(() => buildMonthView(state, month), [state, month]);

	function openEditor(occurrence: Occurrence) {
		const template = state.templates.find(
			(item) => item.id === occurrence.templateId,
		);
		if (template) setEditor({ mode: "edit", occurrence, template });
	}

	const isEmpty = state.templates.length === 0;

	return (
		<main className="page-wrap flex flex-col gap-5 py-8 sm:py-10">
			<div className="rise-in flex flex-wrap items-center justify-between gap-4">
				<div>
					<p className="island-kicker">Finanças como tarefas</p>
					<h1 className="font-display text-3xl font-bold text-ink">
						Simples Finanças
					</h1>
				</div>
				<div className="flex flex-wrap items-center gap-3">
					<AccountMenu user={user} />
					<MonthNav month={month} onChange={setMonth} />
				</div>
			</div>

			{isEmpty ? (
				<EmptyState onCreate={(kind) => setEditor({ mode: "create", kind })} />
			) : (
				<>
					<BudgetSummary month={month} summary={view.summary} />

					<div className="grid gap-5 lg:grid-cols-2">
						<TaskList
							kind="income"
							title="Receitas"
							occurrences={view.income}
							totalCents={view.summary.incomeTotalCents}
							today={today}
							onToggle={(occurrence) =>
								setOccurrenceDone(
									occurrence.templateId,
									month,
									!occurrence.done,
								)
							}
							onEdit={openEditor}
							onAdd={() => setEditor({ mode: "create", kind: "income" })}
						/>
						<TaskList
							kind="expense"
							title="Despesas"
							occurrences={view.expense}
							totalCents={view.summary.expenseTotalCents}
							today={today}
							onToggle={(occurrence) =>
								setOccurrenceDone(
									occurrence.templateId,
									month,
									!occurrence.done,
								)
							}
							onEdit={openEditor}
							onAdd={() => setEditor({ mode: "create", kind: "expense" })}
						/>
					</div>

					<div className="flex justify-end pb-4 text-xs">
						{confirmingReset ? (
							<span className="flex items-center gap-3 text-ink-soft">
								Apagar tudo e recomeçar?
								<button
									type="button"
									onClick={() => {
										resetAll();
										setConfirmingReset(false);
									}}
									className="font-semibold text-coral hover:underline"
								>
									Sim, apagar
								</button>
								<button
									type="button"
									onClick={() => setConfirmingReset(false)}
									className="font-semibold hover:text-ink"
								>
									Cancelar
								</button>
							</span>
						) : (
							<button
								type="button"
								onClick={() => setConfirmingReset(true)}
								className="text-ink-soft hover:text-ink"
							>
								Apagar todos os dados
							</button>
						)}
					</div>
				</>
			)}

			{editor && (
				<TaskDialog
					key={editor.mode === "edit" ? editor.occurrence.id : "create"}
					editor={editor}
					month={month}
					onClose={() => setEditor(null)}
				/>
			)}
		</main>
	);
}
