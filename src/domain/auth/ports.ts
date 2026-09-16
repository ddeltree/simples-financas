import type {
	AccountSummary,
	PasswordDigest,
	SessionRecord,
	StoredAccount,
} from "./types";

/** Só sabe transformar senha em digest e conferir. Não sabe onde a conta mora. */
export interface PasswordHasher {
	hash(password: string): Promise<PasswordDigest>;
	verify(password: string, digest: PasswordDigest): Promise<boolean>;
}

/**
 * Leitura e escrita de contas são portas separadas: o fluxo de login só precisa ler,
 * e quem só lê não deveria sequer conseguir criar uma conta.
 */
export interface AccountReader {
	findByName(name: string): Promise<StoredAccount | null>;
	findById(id: string): Promise<StoredAccount | null>;
	list(): Promise<AccountSummary[]>;
}

export interface AccountWriter {
	create(account: StoredAccount): Promise<void>;
	update(account: StoredAccount): Promise<void>;
}

export interface SessionStore {
	read(): SessionRecord | null;
	write(record: SessionRecord): void;
	clear(): void;
}

/** Existe para que a expiração de sessão seja verificável sem esperar 12 horas. */
export interface Clock {
	now(): number;
}

export interface IdGenerator {
	newId(): string;
}
