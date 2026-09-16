import { Trash2, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import type { Occurrence, TaskKind, TaskTemplate } from "#/domain/types";
import { centsToInputValue, parseAmountToCents } from "#/lib/money";
import { formatMonthLabel, type MonthKey } from "#/lib/month";
import { cn } from "#/lib/utils";
import {
	addTemplate,
	deleteTemplate,
	endTemplateAfter,
	setOccurrenceAmount,
	updateTemplate,
} from "#/store/finance-store";

/** Alvo do diálogo: criar uma tarefa nova ou editar a ocorrência aberta. */
export type TaskEditor =
	| { mode: "create"; kind: TaskKind }
	| { mode: "edit"; occurrence: Occurrence; template: TaskTemplate };

type TaskDialogProps = {
	editor: TaskEditor;
	month: MonthKey;
	onClose: () => void;
};

type Segment<T extends string> = { value: T; label: string };

function SegmentedControl<T extends string>({
	value,
	options,
	onChange,
	label,
}: {
	value: T;
	options: Segment<T>[];
	onChange: (value: T) => void;
	label: string;
}) {
	return (
		<fieldset className="flex min-w-0 gap-1.5 border-0 p-0">
			<legend className="sr-only">{label}</legend>
			{options.map((option) => (
				<button
					key={option.value}
					type="button"
					aria-pressed={value === option.value}
					onClick={() => onChange(option.value)}
					className={cn(
						"flex-1 rounded-xl border px-3 py-2 text-sm font-semibold transition",
						value === option.value
							? "border-lagoon-deep bg-palm-soft text-ink"
							: "border-line bg-[var(--chip-bg)] text-ink-soft hover:text-ink",
					)}
				>
					{option.label}
				</button>
			))}
		</fieldset>
	);
}

const fieldLabel =
	"text-[0.68rem] font-semibold uppercase tracking-[0.12em] text-ink-soft";
const fieldInput =
	"mt-1.5 w-full rounded-xl border border-line bg-white/80 px-3 py-2.5 text-ink outline-none placeholder:text-ink-soft/60 focus:border-lagoon-deep";

export function TaskDialog({ editor, month, onClose }: TaskDialogProps) {
	const ref = useRef<HTMLDialogElement>(null);
	const isEdit = editor.mode === "edit";
	const template = isEdit ? editor.template : null;
	const occurrence = isEdit ? editor.occurrence : null;

	const [kind, setKind] = useState<TaskKind>(
		isEdit ? editor.occurrence.kind : editor.kind,
	);
	const [title, setTitle] = useState(occurrence?.title ?? "");
	const [amount, setAmount] = useState(
		occurrence ? centsToInputValue(occurrence.amountCents) : "",
	);
	const [day, setDay] = useState(String(occurrence?.day ?? 5));
	const [recurring, setRecurring] = useState(occurrence?.recurring ?? true);
	const [scope, setScope] = useState<"all" | "month">(
		occurrence?.adjusted ? "month" : "all",
	);
	const [error, setError] = useState<string | null>(null);
	const [confirmingDelete, setConfirmingDelete] = useState(false);

	useEffect(() => {
		ref.current?.showModal();
	}, []);

	function handleSubmit(event: React.FormEvent) {
		event.preventDefault();

		const trimmed = title.trim();
		if (!trimmed) return setError("Dê um nome para a tarefa.");

		const amountCents = parseAmountToCents(amount);
		if (amountCents === null || amountCents <= 0) {
			return setError("Informe um valor maior que zero.");
		}

		const dayOfMonth = Number(day);
		if (!Number.isInteger(dayOfMonth) || dayOfMonth < 1 || dayOfMonth > 31) {
			return setError("O dia precisa estar entre 1 e 31.");
		}

		const recurrence = {
			type: recurring ? ("monthly" as const) : ("once" as const),
			dayOfMonth,
		};

		if (!template) {
			addTemplate({
				kind,
				title: trimmed,
				amountCents,
				recurrence,
				startMonth: month,
			});
			return onClose();
		}

		const changedRecurrenceType =
			recurring !== (template.recurrence.type === "monthly");

		updateTemplate(template.id, {
			kind,
			title: trimmed,
			recurrence,
			// Trocar o tipo de repetição reancora o modelo no mês que está na tela.
			...(changedRecurrenceType ? { startMonth: month, endMonth: null } : {}),
			...(scope === "all" ? { amountCents } : {}),
		});

		// "Só neste mês" grava um ajuste pontual; "todos os meses" limpa o ajuste anterior.
		setOccurrenceAmount(
			template.id,
			month,
			scope === "month" ? amountCents : null,
		);

		onClose();
	}

	const monthLabel = formatMonthLabel(month);

	return (
		<dialog
			ref={ref}
			onClose={onClose}
			className="m-auto w-[min(30rem,calc(100vw-2rem))] rounded-3xl border border-line bg-transparent p-0 text-ink backdrop:bg-[rgba(23,58,64,0.38)] backdrop:backdrop-blur-[2px]"
		>
			<form
				onSubmit={handleSubmit}
				className="island-shell rounded-3xl p-6"
				noValidate
			>
				<div className="flex items-start justify-between gap-4">
					<div>
						<p className="island-kicker">
							{isEdit ? "Editar tarefa" : "Nova tarefa"}
						</p>
						<h2 className="mt-1 font-display text-2xl font-semibold">
							{isEdit ? occurrence?.title : "O que entra ou sai do mês?"}
						</h2>
					</div>
					<button
						type="button"
						aria-label="Fechar"
						onClick={onClose}
						className="grid size-8 shrink-0 place-items-center rounded-full text-ink-soft hover:bg-[var(--chip-bg)] hover:text-ink"
					>
						<X className="size-4" />
					</button>
				</div>

				<div className="mt-5 flex flex-col gap-4">
					<div>
						<span className={fieldLabel}>Tipo</span>
						<div className="mt-1.5">
							<SegmentedControl
								label="Tipo da tarefa"
								value={kind}
								onChange={setKind}
								options={[
									{ value: "income", label: "Receita" },
									{ value: "expense", label: "Despesa" },
								]}
							/>
						</div>
					</div>

					<label className="block">
						<span className={fieldLabel}>Descrição</span>
						<input
							autoFocus
							value={title}
							onChange={(event) => setTitle(event.target.value)}
							placeholder={kind === "income" ? "Salário" : "Aluguel"}
							className={fieldInput}
						/>
					</label>

					<div className="grid grid-cols-[1fr_7rem] gap-3">
						<label className="block">
							<span className={fieldLabel}>Valor</span>
							<div className="relative">
								<span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 pt-0.5 text-sm text-ink-soft">
									R$
								</span>
								<input
									inputMode="decimal"
									value={amount}
									onChange={(event) => setAmount(event.target.value)}
									placeholder="0,00"
									className={cn(fieldInput, "pl-9 tabular-nums")}
								/>
							</div>
						</label>

						<label className="block">
							<span className={fieldLabel}>Dia</span>
							<input
								type="number"
								min={1}
								max={31}
								value={day}
								onChange={(event) => setDay(event.target.value)}
								className={cn(fieldInput, "tabular-nums")}
							/>
						</label>
					</div>

					<div>
						<span className={fieldLabel}>Repetição</span>
						<div className="mt-1.5">
							<SegmentedControl
								label="Repetição"
								value={recurring ? "monthly" : "once"}
								onChange={(value) => setRecurring(value === "monthly")}
								options={[
									{ value: "monthly", label: "Todo mês" },
									{ value: "once", label: `Só em ${monthLabel.split(" ")[0]}` },
								]}
							/>
						</div>
					</div>

					{isEdit && recurring && (
						<div>
							<span className={fieldLabel}>O novo valor vale para</span>
							<div className="mt-1.5">
								<SegmentedControl
									label="Alcance do novo valor"
									value={scope}
									onChange={setScope}
									options={[
										{ value: "all", label: "Todos os meses" },
										{
											value: "month",
											label: `Só ${monthLabel.split(" ")[0].toLowerCase()}`,
										},
									]}
								/>
							</div>
						</div>
					)}
				</div>

				{error && (
					<p role="alert" className="mt-4 text-sm font-medium text-coral">
						{error}
					</p>
				)}

				{confirmingDelete && template ? (
					<div className="mt-5 rounded-2xl border border-coral/40 bg-coral-soft p-4">
						<p className="text-sm font-semibold">Como quer remover?</p>
						<div className="mt-3 flex flex-col gap-2">
							{template.recurrence.type === "monthly" && (
								<button
									type="button"
									onClick={() => {
										endTemplateAfter(template.id, month);
										onClose();
									}}
									className="rounded-xl border border-line bg-[var(--chip-bg)] px-3 py-2 text-sm font-semibold hover:border-lagoon-deep"
								>
									Encerrar depois de {monthLabel.toLowerCase()}
								</button>
							)}
							<button
								type="button"
								onClick={() => {
									deleteTemplate(template.id);
									onClose();
								}}
								className="rounded-xl bg-coral px-3 py-2 text-sm font-semibold text-white hover:opacity-90"
							>
								Excluir de todos os meses
							</button>
							<button
								type="button"
								onClick={() => setConfirmingDelete(false)}
								className="px-3 py-1 text-sm font-semibold text-ink-soft hover:text-ink"
							>
								Cancelar
							</button>
						</div>
					</div>
				) : (
					<div className="mt-6 flex items-center justify-between gap-3">
						{isEdit ? (
							<button
								type="button"
								onClick={() => setConfirmingDelete(true)}
								className="inline-flex items-center gap-1.5 rounded-xl px-2 py-2 text-sm font-semibold text-ink-soft hover:text-coral"
							>
								<Trash2 className="size-4" /> Remover
							</button>
						) : (
							<span />
						)}

						<div className="flex items-center gap-2">
							<button
								type="button"
								onClick={onClose}
								className="rounded-xl px-3 py-2.5 text-sm font-semibold text-ink-soft hover:text-ink"
							>
								Cancelar
							</button>
							<button
								type="submit"
								className="rounded-xl bg-ink px-5 py-2.5 text-sm font-semibold text-[var(--foam)] hover:opacity-90"
							>
								Salvar
							</button>
						</div>
					</div>
				)}
			</form>
		</dialog>
	);
}
