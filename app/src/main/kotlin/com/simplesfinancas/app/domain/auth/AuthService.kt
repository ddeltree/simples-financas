package com.simplesfinancas.app.domain.auth

import java.time.Instant

private const val HOUR_MS: Long = 60 * 60 * 1000

/** Sessão comum: dura o dia de trabalho. */
const val SESSION_TTL_MS: Long = 12 * HOUR_MS

/** "Manter conectado neste dispositivo". */
const val REMEMBER_TTL_MS: Long = 30 * 24 * HOUR_MS

data class CredentialsInput(
	val name: String,
	val password: String,
	val remember: Boolean = false,
)

/**
 * Toda a regra de autenticação, e só ela. Não conhece Compose, DataStore, PBKDF2 nem
 * o formato em que a conta é gravada — recebe tudo isso como portas no construtor.
 */
class AuthService(
	private val hasher: PasswordHasher,
	private val accountReader: AccountReader,
	private val accountWriter: AccountWriter,
	private val sessions: SessionStore,
	private val clock: Clock,
	private val ids: IdGenerator,
) {
	suspend fun listAccounts(): List<AccountSummary> = accountReader.list()

	suspend fun register(input: CredentialsInput): AuthResult<AccountSummary> {
		val name = input.name.trim()
		if (name.isEmpty()) {
			return AuthResult.Err(authError(AuthErrorCode.NOME_OBRIGATORIO))
		}
		if (input.password.length < MIN_PASSWORD_LENGTH) {
			return AuthResult.Err(authError(AuthErrorCode.SENHA_CURTA))
		}

		if (accountReader.findByName(name) != null) {
			return AuthResult.Err(authError(AuthErrorCode.NOME_EM_USO))
		}

		val account = StoredAccount(
			id = ids.newId(),
			name = name,
			createdAt = Instant.ofEpochMilli(clock.now()).toString(),
			password = hasher.hash(input.password),
		)

		accountWriter.create(account)
		openSession(account.id, input.remember)

		return AuthResult.Ok(account.toSummary())
	}

	suspend fun signIn(input: CredentialsInput): AuthResult<AccountSummary> {
		val account = accountReader.findByName(input.name)

		if (account == null) {
			// Gasta o mesmo tempo de uma verificação real, para o erro não denunciar
			// pela demora se aquele nome existe ou não.
			hasher.hash(input.password)
			return AuthResult.Err(authError(AuthErrorCode.CREDENCIAIS_INVALIDAS))
		}

		if (!hasher.verify(input.password, account.password)) {
			return AuthResult.Err(authError(AuthErrorCode.CREDENCIAIS_INVALIDAS))
		}

		openSession(account.id, input.remember)
		return AuthResult.Ok(account.toSummary())
	}

	suspend fun signOut() {
		sessions.clear()
	}

	/** Devolve quem está logado, ou `null` se não há sessão válida. */
	suspend fun restore(): AccountSummary? {
		val record = sessions.read() ?: return null

		if (record.expiresAt <= clock.now()) {
			sessions.clear()
			return null
		}

		val account = accountReader.findById(record.userId)
		if (account == null) {
			sessions.clear()
			return null
		}

		return account.toSummary()
	}

	private suspend fun openSession(userId: String, remember: Boolean) {
		val startedAt = clock.now()
		sessions.write(
			SessionRecord(
				userId = userId,
				startedAt = startedAt,
				expiresAt = startedAt + if (remember) REMEMBER_TTL_MS else SESSION_TTL_MS,
			),
		)
	}
}
