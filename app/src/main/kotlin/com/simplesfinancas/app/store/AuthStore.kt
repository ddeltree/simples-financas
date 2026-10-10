package com.simplesfinancas.app.store

import com.simplesfinancas.app.domain.auth.AccountSummary
import com.simplesfinancas.app.domain.auth.AuthError
import com.simplesfinancas.app.domain.auth.AuthResult
import com.simplesfinancas.app.domain.auth.AuthService
import com.simplesfinancas.app.domain.auth.CredentialsInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthStatus { LOADING, SIGNED_OUT, SIGNED_IN }

data class AuthSnapshot(
	val status: AuthStatus = AuthStatus.LOADING,
	val user: AccountSummary? = null,
	val accounts: List<AccountSummary> = emptyList(),
	val pending: Boolean = false,
	val error: AuthError? = null,
)

/** A sessão como a UI a enxerga. A regra mora no [AuthService]; aqui é só estado de tela. */
class AuthStore(
	private val authService: AuthService,
	private val scope: CoroutineScope,
) {
	private val _state = MutableStateFlow(AuthSnapshot())
	val state: StateFlow<AuthSnapshot> = _state.asStateFlow()

	init {
		scope.launch { restoreSession() }
	}

	private suspend fun restoreSession() {
		val user = authService.restore()
		val accounts = authService.listAccounts()

		_state.update {
			it.copy(
				status = if (user == null) AuthStatus.SIGNED_OUT else AuthStatus.SIGNED_IN,
				user = user,
				accounts = accounts,
				pending = false,
				error = null,
			)
		}
	}

	private suspend fun authenticate(
		input: CredentialsInput,
		run: suspend (CredentialsInput) -> AuthResult<AccountSummary>,
	): Boolean {
		_state.update { it.copy(pending = true, error = null) }

		return when (val result = run(input)) {
			is AuthResult.Err -> {
				_state.update { it.copy(pending = false, error = result.error) }
				false
			}

			is AuthResult.Ok -> {
				val accounts = authService.listAccounts()
				_state.update {
					it.copy(
						status = AuthStatus.SIGNED_IN,
						user = result.value,
						accounts = accounts,
						pending = false,
						error = null,
					)
				}
				true
			}
		}
	}

	fun signIn(input: CredentialsInput) {
		scope.launch { authenticate(input) { authService.signIn(it) } }
	}

	fun register(input: CredentialsInput) {
		scope.launch { authenticate(input) { authService.register(it) } }
	}

	fun signOut() {
		scope.launch {
			authService.signOut()
			_state.update {
				it.copy(
					status = AuthStatus.SIGNED_OUT,
					user = null,
					pending = false,
					error = null,
				)
			}
		}
	}

	fun clearError() {
		if (_state.value.error != null) _state.update { it.copy(error = null) }
	}
}
