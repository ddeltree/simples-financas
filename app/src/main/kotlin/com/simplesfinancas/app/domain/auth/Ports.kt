package com.simplesfinancas.app.domain.auth

/** Só sabe transformar senha em digest e conferir. Não sabe onde a conta mora. */
interface PasswordHasher {
	suspend fun hash(password: String): PasswordDigest

	suspend fun verify(password: String, digest: PasswordDigest): Boolean
}

/**
 * Leitura e escrita de contas são portas separadas: o fluxo de login só precisa ler,
 * e quem só lê não deveria sequer conseguir criar uma conta.
 */
interface AccountReader {
	suspend fun findByName(name: String): StoredAccount?

	suspend fun findById(id: String): StoredAccount?

	suspend fun list(): List<AccountSummary>
}

interface AccountWriter {
	suspend fun create(account: StoredAccount)

	suspend fun update(account: StoredAccount)
}

/** Suspensa, diferente do web: no Android o armazenamento (DataStore) é assíncrono. */
interface SessionStore {
	suspend fun read(): SessionRecord?

	suspend fun write(record: SessionRecord)

	suspend fun clear()
}

/** Existe para que a expiração de sessão seja verificável sem esperar 12 horas. */
fun interface Clock {
	fun now(): Long
}

fun interface IdGenerator {
	fun newId(): String
}
