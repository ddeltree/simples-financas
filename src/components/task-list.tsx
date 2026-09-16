import { Check, Pencil, Plus, Repeat } from "lucide-react";
import type { Occurrence, TaskKind } from "#/domain/types";
import { formatBRL } from "#/lib/money";
import { cn } from "#/lib/utils";

type TaskListProps = {
	kind: TaskKind;
	title: string;
	occurrences: Occurrence[];
	totalCents: number;
	today: string | null;
	onToggle: (occurrence: Occurrence) => void;
	onEdit: (occurrence: Occurrence) => void;
	onAdd: () => void;
};

function TaskRow({
	occurrence,
	overdue,
	onToggle,
	onEdit,
}: {
	occurrence: Occurrence;
	overdue: boolean;
	onToggle: () => void;
	onEdit: () => void;
}) {
	const isIncome = occurrence.kind === "income";

	return (
		<li className="group flex items-center gap-3 rounded-2xl px-3 py-2.5 transition hover:bg-[var(--link-bg-hover)]">
			<label className="relative shrink-0 cursor-pointer">
				<input
					type="checkbox"
					checked={occurrence.done}
					onChange={onToggle}
					aria-label={`Concluir ${occurrence.title}`}
					className="peer sr-only"
				/>
				<span
					aria-hidden="true"
					className={cn(
						"grid size-6 place-items-center rounded-full border transition peer-focus-visible:ring-2 peer-focus-visible:ring-lagoon-deep peer-focus-visible:ring-offset-2",
						occurrence.done
							? isIncome
								? "border-palm bg-palm text-white"
								: "border-coral bg-coral text-white"
							: "border-[var(--chip-line)] bg-white/70 hover:border-lagoon-deep",
					)}
				>
					<Check
						className={cn(
							"size-3.5 transition-opacity",
							occurrence.done ? "opacity-100" : "opacity-0",
						)}
						strokeWidth={3}
					/>
				</span>
			</label>

			<span
				className={cn(
					"w-9 shrink-0 rounded-lg py-1 text-center text-xs font-bold tabular-nums",
					overdue
						? "bg-coral-soft text-coral"
						: "bg-[var(--chip-bg)] text-ink-soft",
				)}
				title={overdue ? "Venceu e continua pendente" : undefined}
			>
				{String(occurrence.day).padStart(2, "0")}
			</span>

			<span className="min-w-0 flex-1">
				<span
					className={cn(
						"block truncate font-medium text-ink",
						occurrence.done && "line-through opacity-55",
					)}
				>
					{occurrence.title}
				</span>
				{(occurrence.recurring || occurrence.adjusted) && (
					<span className="mt-0.5 flex items-center gap-2 text-[0.68rem] text-ink-soft">
						{occurrence.recurring && (
							<span className="inline-flex items-center gap-1">
								<Repeat className="size-3" /> todo mês
							</span>
						)}
						{occurrence.adjusted && (
							<span className="inline-flex items-center gap-1 text-lagoon-deep">
								valor ajustado neste mês
							</span>
						)}
					</span>
				)}
			</span>

			<span
				className={cn(
					"shrink-0 font-semibold tabular-nums",
					occurrence.done
						? isIncome
							? "text-palm"
							: "text-coral"
						: "text-ink-soft",
				)}
			>
				{isIncome ? "+" : "−"} {formatBRL(occurrence.amountCents)}
			</span>

			<button
				type="button"
				aria-label={`Editar ${occurrence.title}`}
				onClick={onEdit}
				className="grid size-7 shrink-0 place-items-center rounded-full text-ink-soft opacity-0 transition hover:bg-[var(--chip-bg)] hover:text-ink focus-visible:opacity-100 group-hover:opacity-100"
			>
				<Pencil className="size-3.5" />
			</button>
		</li>
	);
}

export function TaskList({
	kind,
	title,
	occurrences,
	totalCents,
	today,
	onToggle,
	onEdit,
	onAdd,
}: TaskListProps) {
	const isIncome = kind === "income";
	const pending = occurrences.filter((item) => !item.done).length;

	return (
		<section className="island-shell flex flex-col rounded-3xl p-5 sm:p-6">
			<header className="flex items-baseline justify-between gap-3 border-b border-line pb-4">
				<div>
					<h2 className="font-display text-xl font-semibold text-ink">
						{title}
					</h2>
					<p className="mt-0.5 text-xs text-ink-soft">
						{occurrences.length === 0
							? "nada por aqui ainda"
							: `${pending} pendente${pending === 1 ? "" : "s"} de ${occurrences.length}`}
					</p>
				</div>
				<span
					className={cn(
						"font-display text-lg font-semibold tabular-nums",
						isIncome ? "text-palm" : "text-coral",
					)}
				>
					{formatBRL(totalCents)}
				</span>
			</header>

			<ul className="-mx-3 mt-2 flex flex-col">
				{occurrences.map((occurrence) => (
					<TaskRow
						key={occurrence.id}
						occurrence={occurrence}
						overdue={
							!occurrence.done && today !== null && occurrence.date < today
						}
						onToggle={() => onToggle(occurrence)}
						onEdit={() => onEdit(occurrence)}
					/>
				))}
			</ul>

			<button
				type="button"
				onClick={onAdd}
				className={cn(
					"mt-3 flex items-center gap-2 rounded-2xl border border-dashed border-[var(--chip-line)] px-3 py-2.5 text-sm font-semibold text-ink-soft transition hover:text-ink",
					isIncome ? "hover:border-palm" : "hover:border-coral",
				)}
			>
				<Plus className="size-4" />
				{isIncome ? "Nova receita" : "Nova despesa"}
			</button>
		</section>
	);
}
