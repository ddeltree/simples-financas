package com.simplesfinancas.app.domain.auth

import kotlinx.serialization.Serializable

/** Tamanho mínimo aceito para a senha de uma conta local. */
const val MIN_PASSWORD_LENGTH: Int = 6

/** O que a UI pode ver de uma conta. Nunca carrega o digest da senha. */
@Serializable
data class AccountSummary(
	val id: String,
	val name: String,
	val createdAt: String,
)

/**
 * Senha derivada. Os parâmetros viajam junto do hash de propósito: se um dia o custo
 * subir, os digests antigos continuam verificáveis com os parâmetros com que nasceram.
 */
@Serializable
data class PasswordDigest(
	val algorithm: String,
	val iterations: Int,
	/** base64 */
	val salt: String,
	/** base64 */
	val hash: String,
)

/** A conta como ela é persistida. Só o repositório e o serviço enxergam isto. */
@Serializable
data class StoredAccount(
	val id: String,
	val name: String,
	val createdAt: String,
	val password: PasswordDigest,
)

fun StoredAccount.toSummary(): AccountSummary = AccountSummary(id, name, createdAt)

@Serializable
data class SessionRecord(
	val userId: String,
	val startedAt: Long,
	val expiresAt: Long,
)

enum class AuthErrorCode(val code: String, val message: String) {
	NOME_OBRIGATORIO("nome-obrigatorio", "Escolha um nome para a conta."),
	NOME_EM_USO("nome-em-uso", "Já existe uma conta com esse nome neste dispositivo."),
	SENHA_CURTA(
		"senha-curta",
		"A senha precisa ter pelo menos $MIN_PASSWORD_LENGTH caracteres.",
	),
	CREDENCIAIS_INVALIDAS("credenciais-invalidas", "Nome ou senha incorretos."),
}

data class AuthError(val code: AuthErrorCode, val message: String)

fun authError(code: AuthErrorCode): AuthError = AuthError(code, code.message)

sealed interface AuthResult<out T> {
	data class Ok<out T>(val value: T) : AuthResult<T>

	data class Err(val error: AuthError) : AuthResult<Nothing>
}
