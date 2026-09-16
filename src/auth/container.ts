import { AuthService } from "#/domain/auth/auth-service";
import type { Clock, IdGenerator } from "#/domain/auth/ports";
import { LocalAccountRepository } from "./local-account-repository";
import { LocalSessionStore } from "./local-session-store";
import { WebCryptoPasswordHasher } from "./webcrypto-hasher";

const systemClock: Clock = { now: () => Date.now() };

const randomIds: IdGenerator = {
	newId: () =>
		typeof crypto !== "undefined" && "randomUUID" in crypto
			? crypto.randomUUID()
			: `u_${Math.random().toString(36).slice(2)}`,
};

const repository = new LocalAccountRepository();

/**
 * Composition root: o único lugar que conhece as implementações concretas. Trocar
 * PBKDF2 por argon2, ou o `localStorage` por um backend, começa e termina aqui.
 */
export const authService = new AuthService({
	hasher: new WebCryptoPasswordHasher(),
	accountReader: repository,
	accountWriter: repository,
	sessions: new LocalSessionStore(),
	clock: systemClock,
	ids: randomIds,
});
