import type { PasswordHasher } from "#/domain/auth/ports";
import type { PasswordDigest } from "#/domain/auth/types";

const ALGORITHM = "PBKDF2-SHA256";
const ITERATIONS = 210_000;
const SALT_BYTES = 16;
const KEY_BITS = 256;

function toBase64(bytes: Uint8Array): string {
	let binary = "";
	for (const byte of bytes) binary += String.fromCharCode(byte);
	return btoa(binary);
}

function fromBase64(value: string): Uint8Array<ArrayBuffer> {
	const binary = atob(value);
	const bytes = new Uint8Array(binary.length);
	for (let i = 0; i < binary.length; i += 1) bytes[i] = binary.charCodeAt(i);
	return bytes;
}

/** Comparação sem retorno antecipado: o tempo não varia com quantos bytes batem. */
function equalBytes(a: Uint8Array, b: Uint8Array): boolean {
	if (a.length !== b.length) return false;
	let diff = 0;
	for (let i = 0; i < a.length; i += 1) diff |= (a[i] ?? 0) ^ (b[i] ?? 0);
	return diff === 0;
}

async function derive(
	password: string,
	salt: Uint8Array<ArrayBuffer>,
	iterations: number,
): Promise<Uint8Array<ArrayBuffer>> {
	const material = await crypto.subtle.importKey(
		"raw",
		new TextEncoder().encode(password),
		"PBKDF2",
		false,
		["deriveBits"],
	);

	const bits = await crypto.subtle.deriveBits(
		{ name: "PBKDF2", salt, iterations, hash: "SHA-256" },
		material,
		KEY_BITS,
	);

	return new Uint8Array(bits);
}

/**
 * `PasswordHasher` sobre a WebCrypto — a mesma API existe no browser e no Node, então
 * este adaptador também roda nos scripts de verificação com `jiti`.
 */
export class WebCryptoPasswordHasher implements PasswordHasher {
	async hash(password: string): Promise<PasswordDigest> {
		const salt = crypto.getRandomValues(new Uint8Array(SALT_BYTES));
		const derived = await derive(password, salt, ITERATIONS);

		return {
			algorithm: ALGORITHM,
			iterations: ITERATIONS,
			salt: toBase64(salt),
			hash: toBase64(derived),
		};
	}

	async verify(password: string, digest: PasswordDigest): Promise<boolean> {
		if (digest.algorithm !== ALGORITHM) return false;

		// Usa os parâmetros gravados, não os atuais: digest antigo continua conferindo.
		const derived = await derive(
			password,
			fromBase64(digest.salt),
			digest.iterations,
		);

		return equalBytes(derived, fromBase64(digest.hash));
	}
}
