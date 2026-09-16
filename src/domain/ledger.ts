import type {
	FinanceState,
	MonthSummary,
	MonthView,
	Occurrence,
	TaskTemplate,
} from "#/domain/types";
import { clampDay, isoDate, type MonthKey } from "#/lib/month";

export function occurrenceKey(templateId: string, month: MonthKey): string {
	return `${templateId}:${month}`;
}

/** Modelos "once" valem só no mês de origem; "monthly" vale da origem até o fim (se houver). */
export function occursIn(template: TaskTemplate, month: MonthKey): boolean {
	if (template.recurrence.type === "once") return template.startMonth === month;
	if (month < template.startMonth) return false;
	if (template.endMonth && month > template.endMonth) return false;
	return true;
}

function toOccurrence(
	template: TaskTemplate,
	month: MonthKey,
	state: FinanceState,
): Occurrence {
	const stored = state.occurrences[occurrenceKey(template.id, month)];
	const amountCents = stored?.amountCents ?? template.amountCents;
	const day = clampDay(month, template.recurrence.dayOfMonth);

	return {
		id: occurrenceKey(template.id, month),
		templateId: template.id,
		month,
		kind: template.kind,
		title: template.title,
		amountCents,
		day,
		date: isoDate(month, day),
		done: stored?.done ?? false,
		doneAt: stored?.doneAt ?? null,
		recurring: template.recurrence.type === "monthly",
		adjusted: amountCents !== template.amountCents,
	};
}

function byDayThenTitle(a: Occurrence, b: Occurrence): number {
	return a.day - b.day || a.title.localeCompare(b.title, "pt-BR");
}

function sumCents(occurrences: Occurrence[]): number {
	return occurrences.reduce((total, item) => total + item.amountCents, 0);
}

export function summarize(
	income: Occurrence[],
	expense: Occurrence[],
): MonthSummary {
	const incomeDone = income.filter((item) => item.done);
	const expenseDone = expense.filter((item) => item.done);

	const incomeDoneCents = sumCents(incomeDone);
	const incomeTotalCents = sumCents(income);
	const expenseDoneCents = sumCents(expenseDone);
	const expenseTotalCents = sumCents(expense);

	return {
		incomeDoneCents,
		incomePendingCents: incomeTotalCents - incomeDoneCents,
		incomeTotalCents,
		expenseDoneCents,
		expensePendingCents: expenseTotalCents - expenseDoneCents,
		expenseTotalCents,
		availableCents: incomeDoneCents - expenseDoneCents,
		projectedCents: incomeTotalCents - expenseTotalCents,
		tasksDone: incomeDone.length + expenseDone.length,
		tasksTotal: income.length + expense.length,
	};
}

/** Ponto único de leitura da tela: dos modelos guardados para as tarefas de um mês. */
export function buildMonthView(
	state: FinanceState,
	month: MonthKey,
): MonthView {
	const occurrences = state.templates
		.filter((template) => occursIn(template, month))
		.map((template) => toOccurrence(template, month, state));

	const income = occurrences
		.filter((item) => item.kind === "income")
		.sort(byDayThenTitle);
	const expense = occurrences
		.filter((item) => item.kind === "expense")
		.sort(byDayThenTitle);

	return { month, income, expense, summary: summarize(income, expense) };
}
