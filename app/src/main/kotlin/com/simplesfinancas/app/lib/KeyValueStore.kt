package com.simplesfinancas.app.lib

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.first

/**
 * O `localStorage` do app: um saco só de chaves string, compartilhado pelos adaptadores.
 * Mantém as mesmas chaves do web (`simples-financas:…`) e a mesma postura diante de
 * falha de escrita — engole, porque não há o que fazer além de seguir com o que está na memória.
 */
class KeyValueStore(private val dataStore: DataStore<Preferences>) {
	suspend fun read(key: String): String? =
		try {
			dataStore.data.first()[stringPreferencesKey(key)]
		} catch (_: IOException) {
			null
		}

	suspend fun write(key: String, value: String) {
		try {
			dataStore.edit { it[stringPreferencesKey(key)] = value }
		} catch (_: IOException) {
			// Armazenamento cheio ou indisponível: o estado em memória segue valendo.
		}
	}

	suspend fun remove(key: String) {
		try {
			dataStore.edit { it.remove(stringPreferencesKey(key)) }
		} catch (_: IOException) {
			// Nada a fazer; quem chamou já limpou o estado em memória.
		}
	}
}
