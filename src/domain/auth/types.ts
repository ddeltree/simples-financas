/** Tamanho mínimo aceito para a senha de uma conta local. */
export const MIN_PASSWORD_LENGTH = 6;

/** O que a UI pode ver de uma conta. Nunca carrega o digest da senha. */
export type AccountSummary = {
	id: string;
	name: string;
	createdAt: string;
};

/**
 * Senha derivada. Os parâmetros viajam junto do hash de propósito: se um dia o custo
 * subir, os digests antigos continuam verificáveis com os parâmetros com que nasceram.
 */
export type PasswordDigest = {
	algorithm: "PBKDF2-SHA256";
	iterations: number;
	/** base64 */
	salt: string;
	/** base64 */
	hash: string;
};

/** A conta como ela é persistida. Só o repositório e o serviço enxergam isto. */
export type StoredAccount = AccountSummary & {
	password: PasswordDigest;
};

export type SessionRecord = {
	userId: string;
	startedAt: number;
	expiresAt: number;
};

export type AuthErrorCode =
	| "nome-obrigatorio"
	| "nome-em-uso"
	| "senha-curta"
	| "credenciais-invalidas";

export type AuthError = {
	code: AuthErrorCode;
	message: string;
};

export type AuthResult<T> =
	| { ok: true; value: T }
	| { ok: false; error: AuthError };

const MESSAGES: Record<AuthErrorCode, string> = {
	"nome-obrigatorio": "Escolha um nome para a conta.",
	"nome-em-uso": "Já existe uma conta com esse nome neste dispositivo.",
	"senha-curta": `A senha precisa ter pelo menos ${MIN_PASSWORD_LENGTH} caracteres.`,
	"credenciais-invalidas": "Nome ou senha incorretos.",
};

export function authError(code: AuthErrorCode): AuthError {
	return { code, message: MESSAGES[code] };
}
