package com.simplesfinancas.app.auth

import com.simplesfinancas.app.domain.auth.SessionRecord
import com.simplesfinancas.app.domain.auth.SessionStore
import com.simplesfinancas.app.lib.AppJson
import com.simplesfinancas.app.lib.KeyValueStore

private const val STORAGE_KEY = "simples-financas:session:v1"

class DataStoreSessionStore(private val storage: KeyValueStore) : SessionStore {
	override suspend fun read(): SessionRecord? {
		val raw = storage.read(STORAGE_KEY) ?: return null
		return runCatching { AppJson.decodeFromString<SessionRecord>(raw) }.getOrNull()
	}

	override suspend fun write(record: SessionRecord) {
		storage.write(STORAGE_KEY, AppJson.encodeToString(record))
	}

	override suspend fun clear() {
		storage.remove(STORAGE_KEY)
	}
}
