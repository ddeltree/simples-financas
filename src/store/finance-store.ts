import { useSyncExternalStore } from "react";
import { occurrenceKey } from "#/domain/ledger";
import type {
	FinanceState,
	Recurrence,
	TaskKind,
	TaskTemplate,
} from "#/domain/types";
import { currentMonthKey, type MonthKey } from "#/lib/month";

const STORAGE_PREFIX = "simples-financas:v1";

/** Chave usada antes de existirem contas — adotada pelo primeiro usuário que entrar. */
const LEGACY_STORAGE_KEY = STORAGE_PREFIX;

const EMPTY: FinanceState = { templates: [], occurrences: {} };

let state: FinanceState = EMPTY;
let hydrated = false;
/** Usuário dono dos dados em memória. `null` = ninguém logado, nada a ler nem gravar. */
let scope: string | null = null;
const listeners = new Set<() => void>();

function isBrowser(): boolean {
	return typeof window !== "undefined";
}

function storageKey(): string | null {
	return scope === null ? null : `${STORAGE_PREFIX}:u:${scope}`;
}

function parseState(raw: string | null): FinanceState | null {
	if (!raw) return null;

	try {
		const parsed = JSON.parse(raw) as Partial<FinanceState>;
		return {
			templates: parsed.templates ?? [],
			occurrences: parsed.occurrences ?? {},
		};
	} catch {
		return null;
	}
}

function readStorage(): FinanceState {
	const key = storageKey();
	if (key === null || !isBrowser()) return EMPTY;

	try {
		return parseState(window.localStorage.getItem(key)) ?? EMPTY;
	} catch {
		return EMPTY;
	}
}

function writeStorage(next: FinanceState): void {
	const key = storageKey();
	if (key === null || !isBrowser()) return;

	try {
		window.localStorage.setItem(key, JSON.stringify(next));
	} catch {
		// cota cheia ou storage bloqueado: o estado em memória segue valendo nesta sessão.
	}
}

/**
 * Dados do protótipo pré-contas: o primeiro usuário que abrir uma gaveta vazia herda o
 * que estava na chave antiga. Como a chave é removida em seguida, só acontece uma vez.
 */
function adoptLegacyState(key: string): FinanceState | null {
	try {
		if (window.localStorage.getItem(key) !== null) return null;

		const legacy = parseState(window.localStorage.getItem(LEGACY_STORAGE_KEY));
		if (legacy === null) return null;

		window.localStorage.setItem(key, JSON.stringify(legacy));
		window.localStorage.removeItem(LEGACY_STORAGE_KEY);
		return legacy;
	} catch {
		return null;
	}
}

/**
 * Aponta a store para a gaveta de um usuário (ou para lugar nenhum, no logout). Quem
 * chama é a ponte de sessão — esta store não conhece autenticação.
 */
export function setStorageScope(next: string | null): void {
	if (scope === next) return;

	scope = next;
	hydrated = true;

	const key = storageKey();
	state =
		key !== null && isBrowser()
			? (adoptLegacyState(key) ?? readStorage())
			: EMPTY;

	emit();
}

function emit(): void {
	for (const listener of listeners) listener();
}

function setState(next: FinanceState): void {
	state = next;
	if (isBrowser()) writeStorage(next);
	emit();
}

function subscribe(listener: () => void): () => void {
	// A hidratação acontece no primeiro subscribe (já no cliente), nunca durante o render.
	if (!hydrated && isBrowser()) {
		hydrated = true;
		state = readStorage();
	}

	listeners.add(listener);

	if (listeners.size === 1 && isBrowser()) {
		window.addEventListener("storage", onExternalChange);
	}

	return () => {
		listeners.delete(listener);
		if (listeners.size === 0 && isBrowser()) {
			window.removeEventListener("storage", onExternalChange);
		}
	};
}

/** Mantém abas abertas em sincronia. */
function onExternalChange(event: StorageEvent): void {
	if (event.key !== null && event.key !== storageKey()) return;
	state = readStorage();
	emit();
}

function getSnapshot(): FinanceState {
	return state;
}

function getServerSnapshot(): FinanceState {
	return EMPTY;
}

export function useFinanceState(): FinanceState {
	return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
}

/** O mesmo estado, para quem lê fora do React (scripts, pontes). */
export function getFinanceState(): FinanceState {
	return state;
}

function newId(): string {
	if (isBrowser() && "randomUUID" in crypto) return crypto.randomUUID();
	return `t_${Math.random().toString(36).slice(2)}`;
}

export type TemplateInput = {
	kind: TaskKind;
	title: string;
	amountCents: number;
	recurrence: Recurrence;
	startMonth: MonthKey;
};

export function addTemplate(input: TemplateInput): void {
	const template: TaskTemplate = {
		id: newId(),
		kind: input.kind,
		title: input.title.trim(),
		amountCents: input.amountCents,
		recurrence: input.recurrence,
		startMonth: input.startMonth,
		endMonth: null,
		createdAt: new Date().toISOString(),
	};

	setState({ ...state, templates: [...state.templates, template] });
}

export function updateTemplate(
	id: string,
	patch: Partial<Omit<TaskTemplate, "id" | "createdAt">>,
): void {
	setState({
		...state,
		templates: state.templates.map((template) =>
			template.id === id ? { ...template, ...patch } : template,
		),
	});
}

export function deleteTemplate(id: string): void {
	const occurrences: FinanceState["occurrences"] = {};
	for (const [key, value] of Object.entries(state.occurrences)) {
		if (!key.startsWith(`${id}:`)) occurrences[key] = value;
	}

	setState({
		templates: state.templates.filter((template) => template.id !== id),
		occurrences,
	});
}

/** Encerra um modelo recorrente a partir do mês seguinte, preservando o histórico já concluído. */
export function endTemplateAfter(id: string, month: MonthKey): void {
	updateTemplate(id, { endMonth: month });
}

function patchOccurrence(
	templateId: string,
	month: MonthKey,
	patch: Partial<FinanceState["occurrences"][string]>,
): void {
	const key = occurrenceKey(templateId, month);
	const current = state.occurrences[key] ?? { done: false, doneAt: null };

	setState({
		...state,
		occurrences: { ...state.occurrences, [key]: { ...current, ...patch } },
	});
}

export function setOccurrenceDone(
	templateId: string,
	month: MonthKey,
	done: boolean,
): void {
	patchOccurrence(templateId, month, {
		done,
		doneAt: done ? new Date().toISOString() : null,
	});
}

/** Ajuste só deste mês; `null` volta a seguir o valor do modelo. */
export function setOccurrenceAmount(
	templateId: string,
	month: MonthKey,
	amountCents: number | null,
): void {
	const key = occurrenceKey(templateId, month);
	const current = state.occurrences[key] ?? { done: false, doneAt: null };
	const next = { ...current };

	if (amountCents === null) delete next.amountCents;
	else next.amountCents = amountCents;

	setState({ ...state, occurrences: { ...state.occurrences, [key]: next } });
}

export function resetAll(): void {
	setState(EMPTY);
}

/** Dados de demonstração para quem abre o app pela primeira vez. */
export function seedExamples(): void {
	const startMonth = currentMonthKey();
	const createdAt = new Date().toISOString();

	const make = (
		kind: TaskKind,
		title: string,
		amount: number,
		dayOfMonth: number,
	): TaskTemplate => ({
		id: newId(),
		kind,
		title,
		amountCents: amount,
		recurrence: { type: "monthly", dayOfMonth },
		startMonth,
		endMonth: null,
		createdAt,
	});

	setState({
		occurrences: {},
		templates: [
			make("income", "Salário", 480000, 5),
			make("income", "Freela", 60000, 20),
			make("expense", "Aluguel", 150000, 10),
			make("expense", "Cartão de crédito", 120000, 14),
			make("expense", "Mercado", 90000, 8),
			make("expense", "Energia", 18000, 12),
			make("expense", "Internet", 12000, 15),
			make("expense", "Academia", 11000, 3),
		],
	});
}
