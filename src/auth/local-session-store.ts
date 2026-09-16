import type { SessionStore } from "#/domain/auth/ports";
import type { SessionRecord } from "#/domain/auth/types";

const STORAGE_KEY = "simples-financas:session:v1";

function isBrowser(): boolean {
	return typeof window !== "undefined";
}

export class LocalSessionStore implements SessionStore {
	read(): SessionRecord | null {
		if (!isBrowser()) return null;

		try {
			const raw = window.localStorage.getItem(STORAGE_KEY);
			if (!raw) return null;
			const parsed = JSON.parse(raw) as Partial<SessionRecord>;
			if (typeof parsed.userId !== "string") return null;
			if (typeof parsed.expiresAt !== "number") return null;

			return {
				userId: parsed.userId,
				startedAt: parsed.startedAt ?? parsed.expiresAt,
				expiresAt: parsed.expiresAt,
			};
		} catch {
			return null;
		}
	}

	write(record: SessionRecord): void {
		if (!isBrowser()) return;

		try {
			window.localStorage.setItem(STORAGE_KEY, JSON.stringify(record));
		} catch {
			// Sem persistência: a sessão vale enquanto a aba estiver aberta.
		}
	}

	clear(): void {
		if (!isBrowser()) return;

		try {
			window.localStorage.removeItem(STORAGE_KEY);
		} catch {
			// Nada a fazer; o estado em memória já foi limpo por quem chamou.
		}
	}
}
