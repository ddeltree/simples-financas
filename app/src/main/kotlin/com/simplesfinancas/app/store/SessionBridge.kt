package com.simplesfinancas.app.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Inicializa a gaveta de armazenamento do FinanceStore.
 * Sem autenticação necessária: usa a conta salva se houver, ou 'local' como padrão.
 */
fun startSessionBridge(
	authStore: AuthStore,
	financeStore: FinanceStore,
	scope: CoroutineScope,
) {
	scope.launch {
		authStore.state.collect { snapshot ->
			if (snapshot.status != AuthStatus.LOADING) {
				val scopeId = snapshot.user?.id
					?: snapshot.accounts.firstOrNull()?.id
					?: "local"
				financeStore.setStorageScope(scopeId)
			}
		}
	}
}
