package com.simplesfinancas.app.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A única peça que conhece as duas stores. A financeira não sabe o que é uma conta e a
 * de autenticação não sabe o que é um orçamento; a ponte traduz "quem entrou" em "qual
 * gaveta abrir".
 */
fun startSessionBridge(
	authStore: AuthStore,
	financeStore: FinanceStore,
	scope: CoroutineScope,
) {
	scope.launch {
		authStore.state
			.map { snapshot ->
				if (snapshot.status == AuthStatus.SIGNED_IN) snapshot.user?.id else null
			}
			.distinctUntilChanged()
			.collect { financeStore.setStorageScope(it) }
	}
}
