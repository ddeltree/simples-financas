package com.simplesfinancas.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.simplesfinancas.app.lib.KeyValueStore
import com.simplesfinancas.app.store.FinanceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.preferences: DataStore<Preferences> by preferencesDataStore(
	name = "simples-financas",
)

/**
 * Composition root: o único lugar que conhece as implementações concretas. Trocar o
 * armazenamento local por um backend começa e termina aqui.
 */
class AppContainer(context: Context) {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

	private val storage = KeyValueStore(context.applicationContext.preferences)

	val financeStore = FinanceStore(storage, scope)
}
