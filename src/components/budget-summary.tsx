import type { MonthSummary } from "#/domain/types";
import { formatBRL } from "#/lib/money";
import { formatMonthLabel, type MonthKey } from "#/lib/month";
import { cn } from "#/lib/utils";

type BudgetSummaryProps = {
	month: MonthKey;
	summary: MonthSummary;
};

function Stat({
	label,
	value,
	tone,
}: {
	label: string;
	value: string;
	tone: "income" | "expense" | "neutral";
}) {
	return (
		<div className="rounded-2xl border border-line bg-[var(--chip-bg)] px-4 py-3">
			<p className="text-[0.68rem] font-semibold uppercase tracking-[0.12em] text-ink-soft">
				{label}
			</p>
			<p
				className={cn(
					"mt-1 font-display text-lg font-semibold tabular-nums",
					tone === "income" && "text-palm",
					tone === "expense" && "text-coral",
					tone === "neutral" && "text-ink",
				)}
			>
				{value}
			</p>
		</div>
	);
}

export function BudgetSummary({ month, summary }: BudgetSummaryProps) {
	const progress =
		summary.tasksTotal === 0
			? 0
			: Math.round((summary.tasksDone / summary.tasksTotal) * 100);

	return (
		<section className="island-shell rounded-3xl p-6 sm:p-8">
			<div className="flex flex-wrap items-end justify-between gap-6">
				<div>
					<p className="island-kicker">
						Orçamento de {formatMonthLabel(month).toLowerCase()}
					</p>
					<p
						className={cn(
							"mt-2 font-display text-4xl font-bold tabular-nums sm:text-5xl",
							summary.availableCents < 0 ? "text-coral" : "text-ink",
						)}
					>
						{formatBRL(summary.availableCents)}
					</p>
					<p className="mt-1 text-sm text-ink-soft">
						disponível agora — o que já entrou menos o que já foi pago
					</p>
				</div>

				<div className="min-w-[13rem] flex-1 sm:max-w-xs">
					<div className="flex items-baseline justify-between text-sm">
						<span className="font-semibold text-ink">
							{summary.tasksDone} de {summary.tasksTotal} tarefas
						</span>
						<span className="tabular-nums text-ink-soft">{progress}%</span>
					</div>
					<div className="mt-2 h-2 overflow-hidden rounded-full bg-[var(--chip-line)]">
						<div
							className="h-full rounded-full bg-gradient-to-r from-lagoon to-palm transition-[width] duration-500"
							style={{ width: `${progress}%` }}
						/>
					</div>
					<p className="mt-3 text-sm text-ink-soft">
						Fim do mês previsto:{" "}
						<strong
							className={cn(
								"font-semibold tabular-nums",
								summary.projectedCents < 0 ? "text-coral" : "text-ink",
							)}
						>
							{formatBRL(summary.projectedCents)}
						</strong>
					</p>
				</div>
			</div>

			<div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
				<Stat
					label="Recebido"
					value={formatBRL(summary.incomeDoneCents)}
					tone="income"
				/>
				<Stat
					label="A receber"
					value={formatBRL(summary.incomePendingCents)}
					tone="neutral"
				/>
				<Stat
					label="Pago"
					value={formatBRL(summary.expenseDoneCents)}
					tone="expense"
				/>
				<Stat
					label="A pagar"
					value={formatBRL(summary.expensePendingCents)}
					tone="neutral"
				/>
			</div>
		</section>
	);
}
