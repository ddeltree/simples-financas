import { normalizeAccountName } from "#/domain/auth/account-name";
import type { AccountReader, AccountWriter } from "#/domain/auth/ports";
import type { AccountSummary, StoredAccount } from "#/domain/auth/types";

const STORAGE_KEY = "simples-financas:auth:v1";

type AccountsFile = { accounts: StoredAccount[] };

function isBrowser(): boolean {
	return typeof window !== "undefined";
}

/** Contas em `localStorage`. Implementa as duas portas; quem consome escolhe qual enxerga. */
export class LocalAccountRepository implements AccountReader, AccountWriter {
	private read(): StoredAccount[] {
		if (!isBrowser()) return [];

		try {
			const raw = window.localStorage.getItem(STORAGE_KEY);
			if (!raw) return [];
			const parsed = JSON.parse(raw) as Partial<AccountsFile>;
			return parsed.accounts ?? [];
		} catch {
			return [];
		}
	}

	private write(accounts: StoredAccount[]): void {
		if (!isBrowser()) return;

		try {
			const file: AccountsFile = { accounts };
			window.localStorage.setItem(STORAGE_KEY, JSON.stringify(file));
		} catch {
			// Storage bloqueado ou cheio: sem onde gravar, a conta vale só nesta aba.
		}
	}

	async findByName(name: string): Promise<StoredAccount | null> {
		const wanted = normalizeAccountName(name);
		const found = this.read().find(
			(account) => normalizeAccountName(account.name) === wanted,
		);
		return found ?? null;
	}

	async findById(id: string): Promise<StoredAccount | null> {
		return this.read().find((account) => account.id === id) ?? null;
	}

	async list(): Promise<AccountSummary[]> {
		return this.read().map(({ id, name, createdAt }) => ({
			id,
			name,
			createdAt,
		}));
	}

	async create(account: StoredAccount): Promise<void> {
		this.write([...this.read(), account]);
	}

	async update(account: StoredAccount): Promise<void> {
		this.write(
			this.read().map((item) => (item.id === account.id ? account : item)),
		);
	}
}
