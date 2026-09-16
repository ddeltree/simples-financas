import type {
	AccountReader,
	AccountWriter,
	Clock,
	IdGenerator,
	PasswordHasher,
	SessionStore,
} from "./ports";
import {
	type AccountSummary,
	type AuthResult,
	authError,
	MIN_PASSWORD_LENGTH,
	type StoredAccount,
} from "./types";

const HOUR_MS = 60 * 60 * 1000;

/** Sessão comum: dura o dia de trabalho. */
export const SESSION_TTL_MS = 12 * HOUR_MS;
/** "Manter conectado neste dispositivo". */
export const REMEMBER_TTL_MS = 30 * 24 * HOUR_MS;

export type CredentialsInput = {
	name: string;
	password: string;
	remember?: boolean;
};

export type AuthServiceDeps = {
	hasher: PasswordHasher;
	accountReader: AccountReader;
	accountWriter: AccountWriter;
	sessions: SessionStore;
	clock: Clock;
	ids: IdGenerator;
};

function toSummary(account: StoredAccount): AccountSummary {
	return {
		id: account.id,
		name: account.name,
		createdAt: account.createdAt,
	};
}

/**
 * Toda a regra de autenticação, e só ela. Não conhece React, `localStorage`, PBKDF2 nem
 * o formato em que a conta é gravada — recebe tudo isso como portas no construtor.
 */
export class AuthService {
	constructor(private readonly deps: AuthServiceDeps) {}

	listAccounts(): Promise<AccountSummary[]> {
		return this.deps.accountReader.list();
	}

	async register(input: CredentialsInput): Promise<AuthResult<AccountSummary>> {
		const name = input.name.trim();
		if (name.length === 0)
			return { ok: false, error: authError("nome-obrigatorio") };
		if (input.password.length < MIN_PASSWORD_LENGTH) {
			return { ok: false, error: authError("senha-curta") };
		}

		const existing = await this.deps.accountReader.findByName(name);
		if (existing !== null)
			return { ok: false, error: authError("nome-em-uso") };

		const account: StoredAccount = {
			id: this.deps.ids.newId(),
			name,
			createdAt: new Date(this.deps.clock.now()).toISOString(),
			password: await this.deps.hasher.hash(input.password),
		};

		await this.deps.accountWriter.create(account);
		this.openSession(account.id, input.remember === true);

		return { ok: true, value: toSummary(account) };
	}

	async signIn(input: CredentialsInput): Promise<AuthResult<AccountSummary>> {
		const account = await this.deps.accountReader.findByName(input.name);

		if (account === null) {
			// Gasta o mesmo tempo de uma verificação real, para o erro não denunciar
			// pela demora se aquele nome existe ou não.
			await this.deps.hasher.hash(input.password);
			return { ok: false, error: authError("credenciais-invalidas") };
		}

		const matches = await this.deps.hasher.verify(
			input.password,
			account.password,
		);
		if (!matches)
			return { ok: false, error: authError("credenciais-invalidas") };

		this.openSession(account.id, input.remember === true);
		return { ok: true, value: toSummary(account) };
	}

	signOut(): void {
		this.deps.sessions.clear();
	}

	/** Devolve quem está logado, ou `null` se não há sessão válida. */
	async restore(): Promise<AccountSummary | null> {
		const record = this.deps.sessions.read();
		if (record === null) return null;

		if (record.expiresAt <= this.deps.clock.now()) {
			this.deps.sessions.clear();
			return null;
		}

		const account = await this.deps.accountReader.findById(record.userId);
		if (account === null) {
			this.deps.sessions.clear();
			return null;
		}

		return toSummary(account);
	}

	private openSession(userId: string, remember: boolean): void {
		const startedAt = this.deps.clock.now();
		this.deps.sessions.write({
			userId,
			startedAt,
			expiresAt: startedAt + (remember ? REMEMBER_TTL_MS : SESSION_TTL_MS),
		});
	}
}
