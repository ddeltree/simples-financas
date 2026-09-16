import type { MonthKey } from "#/lib/month";

/** Receita soma ao orçamento quando concluída; despesa subtrai. */
export type TaskKind = "income" | "expense";

export type Recurrence =
	/** Repete todo mês no mesmo dia, de `startMonth` até `endMonth` (ou indefinidamente). */
	| { type: "monthly"; dayOfMonth: number }
	/** Acontece uma única vez, no mês de `startMonth`. */
	| { type: "once"; dayOfMonth: number };

/**
 * O que o usuário cadastra: o modelo da tarefa.
 * Nenhuma ocorrência é gravada em disco — elas são derivadas do modelo para o mês visitado.
 */
export type TaskTemplate = {
	id: string;
	kind: TaskKind;
	title: string;
	amountCents: number;
	recurrence: Recurrence;
	startMonth: MonthKey;
	endMonth: MonthKey | null;
	createdAt: string;
};

/**
 * O que muda em UMA ocorrência específica (mês X do modelo Y).
 * Guardado por chave `${templateId}:${month}` — só existe quando o usuário mexeu nela.
 */
export type OccurrenceState = {
	done: boolean;
	doneAt: string | null;
	/** Ajuste pontual: "a luz desse mês veio R$ 212,40". */
	amountCents?: number;
};

export type FinanceState = {
	templates: TaskTemplate[];
	/** chave: `${templateId}:${month}` */
	occurrences: Record<string, OccurrenceState>;
};

/** Uma tarefa concreta, já resolvida para um mês. */
export type Occurrence = {
	id: string;
	templateId: string;
	month: MonthKey;
	kind: TaskKind;
	title: string;
	amountCents: number;
	day: number;
	date: string;
	done: boolean;
	doneAt: string | null;
	recurring: boolean;
	/** true quando o valor deste mês difere do valor do modelo. */
	adjusted: boolean;
};

export type MonthSummary = {
	incomeDoneCents: number;
	incomePendingCents: number;
	incomeTotalCents: number;
	expenseDoneCents: number;
	expensePendingCents: number;
	expenseTotalCents: number;
	/** O que de fato entrou menos o que de fato saiu — o saldo do bolso hoje. */
	availableCents: number;
	/** Projeção do fim do mês, contando o que ainda está pendente. */
	projectedCents: number;
	tasksDone: number;
	tasksTotal: number;
};

export type MonthView = {
	month: MonthKey;
	income: Occurrence[];
	expense: Occurrence[];
	summary: MonthSummary;
};
