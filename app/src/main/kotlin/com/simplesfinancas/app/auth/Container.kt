package com.simplesfinancas.app.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.simplesfinancas.app.domain.auth.AuthService
import com.simplesfinancas.app.domain.auth.Clock
import com.simplesfinancas.app.domain.auth.IdGenerator
import com.simplesfinancas.app.lib.KeyValueStore
import com.simplesfinancas.app.store.AuthStore
import com.simplesfinancas.app.store.FinanceStore
import com.simplesfinancas.app.store.startSessionBridge
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.preferences: DataStore<Preferences> by preferencesDataStore(
	name = "simples-financas",
)

private val systemClock = Clock { System.currentTimeMillis() }

private val randomIds = IdGenerator { UUID.randomUUID().toString() }

/**
 * Composition root: o único lugar que conhece as implementações concretas. Trocar
 * PBKDF2 por argon2, ou o armazenamento local por um backend, começa e termina aqui.
 */
class AppContainer(context: Context) {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

	private val storage = KeyValueStore(context.applicationContext.preferences)

	private val repository = DataStoreAccountRepository(storage)

	private val authService = AuthService(
		hasher = Pbkdf2PasswordHasher(),
		accountReader = repository,
		accountWriter = repository,
		sessions = DataStoreSessionStore(storage),
		clock = systemClock,
		ids = randomIds,
	)

	val financeStore = FinanceStore(storage, scope)

	val authStore = AuthStore(authService, scope)

	init {
		startSessionBridge(authStore, financeStore, scope)
	}
}
