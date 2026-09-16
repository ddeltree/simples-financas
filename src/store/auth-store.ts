import { useSyncExternalStore } from "react";
import { authService } from "#/auth/container";
import type { CredentialsInput } from "#/domain/auth/auth-service";
import type { AccountSummary, AuthError } from "#/domain/auth/types";

export type AuthStatus = "loading" | "signed-out" | "signed-in";

export type AuthSnapshot = {
	status: AuthStatus;
	user: AccountSummary | null;
	accounts: AccountSummary[];
	pending: boolean;
	error: AuthError | null;
};

/**
 * Referência estável de propósito: `useSyncExternalStore` compara os snapshots por
 * identidade, e devolver um objeto novo a cada chamada entraria em laço.
 */
const LOADING: AuthSnapshot = Object.freeze({
	status: "loading",
	user: null,
	accounts: [],
	pending: false,
	error: null,
});

let snapshot: AuthSnapshot = LOADING;
let restored = false;
const listeners = new Set<() => void>();

function isBrowser(): boolean {
	return typeof window !== "undefined";
}

function emit(): void {
	for (const listener of listeners) listener();
}

function patch(next: Partial<AuthSnapshot>): void {
	snapshot = { ...snapshot, ...next };
	emit();
}

async function refreshAccounts(): Promise<AccountSummary[]> {
	return authService.listAccounts();
}

async function restoreSession(): Promise<void> {
	const [user, accounts] = await Promise.all([
		authService.restore(),
		refreshAccounts(),
	]);

	patch({
		status: user === null ? "signed-out" : "signed-in",
		user,
		accounts,
		pending: false,
		error: null,
	});
}

function subscribe(listener: () => void): () => void {
	// A leitura do storage acontece no primeiro subscribe (já no cliente), nunca no render.
	if (!restored && isBrowser()) {
		restored = true;
		void restoreSession();
	}

	listeners.add(listener);
	return () => {
		listeners.delete(listener);
	};
}

function getSnapshot(): AuthSnapshot {
	return snapshot;
}

function getServerSnapshot(): AuthSnapshot {
	return LOADING;
}

export function useAuthState(): AuthSnapshot {
	return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
}

/** Para a ponte com a store financeira, que não usa React. */
export function subscribeAuth(listener: () => void): () => void {
	return subscribe(listener);
}

export function getAuthSnapshot(): AuthSnapshot {
	return snapshot;
}

async function authenticate(
	input: CredentialsInput,
	run: (input: CredentialsInput) => ReturnType<typeof authService.signIn>,
): Promise<boolean> {
	patch({ pending: true, error: null });

	const result = await run(input);

	if (!result.ok) {
		patch({ pending: false, error: result.error });
		return false;
	}

	patch({
		status: "signed-in",
		user: result.value,
		accounts: await refreshAccounts(),
		pending: false,
		error: null,
	});

	return true;
}

export function signIn(input: CredentialsInput): Promise<boolean> {
	return authenticate(input, (values) => authService.signIn(values));
}

export function register(input: CredentialsInput): Promise<boolean> {
	return authenticate(input, (values) => authService.register(values));
}

export function signOut(): void {
	authService.signOut();
	patch({ status: "signed-out", user: null, pending: false, error: null });
}

export function clearAuthError(): void {
	if (snapshot.error !== null) patch({ error: null });
}
