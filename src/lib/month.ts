/** Mês no formato "YYYY-MM". Ordenação lexicográfica == ordenação cronológica. */
export type MonthKey = string;

const MONTH_LABEL = new Intl.DateTimeFormat("pt-BR", {
	month: "long",
	year: "numeric",
});

const MONTH_LABEL_SHORT = new Intl.DateTimeFormat("pt-BR", { month: "short" });

function pad(value: number): string {
	return String(value).padStart(2, "0");
}

export function monthKeyOf(date: Date): MonthKey {
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
}

export function currentMonthKey(): MonthKey {
	return monthKeyOf(new Date());
}

export function parseMonthKey(month: MonthKey): {
	year: number;
	index: number;
} {
	const [year, monthNumber] = month.split("-").map(Number);
	return { year, index: monthNumber - 1 };
}

export function addMonths(month: MonthKey, delta: number): MonthKey {
	const { year, index } = parseMonthKey(month);
	return monthKeyOf(new Date(year, index + delta, 1));
}

export function daysInMonth(month: MonthKey): number {
	const { year, index } = parseMonthKey(month);
	return new Date(year, index + 1, 0).getDate();
}

/** "Todo dia 31" em fevereiro vira dia 28/29 — a ocorrência nunca some do mês. */
export function clampDay(month: MonthKey, day: number): number {
	return Math.min(Math.max(Math.trunc(day), 1), daysInMonth(month));
}

export function isoDate(month: MonthKey, day: number): string {
	return `${month}-${pad(clampDay(month, day))}`;
}

export function monthOfIsoDate(date: string): MonthKey {
	return date.slice(0, 7);
}

export function formatMonthLabel(month: MonthKey): string {
	const { year, index } = parseMonthKey(month);
	const label = MONTH_LABEL.format(new Date(year, index, 1));
	return label.charAt(0).toUpperCase() + label.slice(1);
}

export function formatMonthShort(month: MonthKey): string {
	const { year, index } = parseMonthKey(month);
	return MONTH_LABEL_SHORT.format(new Date(year, index, 1)).replace(".", "");
}

export function todayIso(): string {
	const now = new Date();
	return `${monthKeyOf(now)}-${pad(now.getDate())}`;
}
