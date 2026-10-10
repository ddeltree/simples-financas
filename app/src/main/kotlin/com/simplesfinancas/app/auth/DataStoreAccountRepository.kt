package com.simplesfinancas.app.auth

import com.simplesfinancas.app.domain.auth.AccountReader
import com.simplesfinancas.app.domain.auth.AccountSummary
import com.simplesfinancas.app.domain.auth.AccountWriter
import com.simplesfinancas.app.domain.auth.StoredAccount
import com.simplesfinancas.app.domain.auth.normalizeAccountName
import com.simplesfinancas.app.domain.auth.toSummary
import com.simplesfinancas.app.lib.AppJson
import com.simplesfinancas.app.lib.KeyValueStore
import kotlinx.serialization.Serializable

private const val STORAGE_KEY = "simples-financas:auth:v1"

@Serializable
private data class AccountsFile(val accounts: List<StoredAccount> = emptyList())

/** Contas no armazenamento local. Implementa as duas portas; quem consome escolhe qual enxerga. */
class DataStoreAccountRepository(private val storage: KeyValueStore) :
	AccountReader,
	AccountWriter {

	private suspend fun read(): List<StoredAccount> {
		val raw = storage.read(STORAGE_KEY) ?: return emptyList()
		return runCatching { AppJson.decodeFromString<AccountsFile>(raw).accounts }
			.getOrDefault(emptyList())
	}

	private suspend fun write(accounts: List<StoredAccount>) {
		storage.write(STORAGE_KEY, AppJson.encodeToString(AccountsFile(accounts)))
	}

	override suspend fun findByName(name: String): StoredAccount? {
		val wanted = normalizeAccountName(name)
		return read().find { normalizeAccountName(it.name) == wanted }
	}

	override suspend fun findById(id: String): StoredAccount? = read().find { it.id == id }

	override suspend fun list(): List<AccountSummary> = read().map { it.toSummary() }

	override suspend fun create(account: StoredAccount) {
		write(read() + account)
	}

	override suspend fun update(account: StoredAccount) {
		write(read().map { if (it.id == account.id) account else it })
	}
}
