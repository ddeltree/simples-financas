package com.simplesfinancas.app.auth

import com.simplesfinancas.app.domain.auth.PasswordDigest
import com.simplesfinancas.app.domain.auth.PasswordHasher
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ALGORITHM = "PBKDF2-SHA256"
private const val ITERATIONS = 210_000
private const val SALT_BYTES = 16
private const val KEY_BITS = 256

private val base64Encoder: Base64.Encoder = Base64.getEncoder()
private val base64Decoder: Base64.Decoder = Base64.getDecoder()

private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
	val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
	return try {
		SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
	} finally {
		spec.clearPassword()
	}
}

/**
 * `PasswordHasher` sobre a JCA. O digest gerado é o mesmo formato do adaptador WebCrypto
 * que existia na versão web — mesmos parâmetros, mesmo base64.
 */
class Pbkdf2PasswordHasher : PasswordHasher {
	private val random = SecureRandom()

	override suspend fun hash(password: String): PasswordDigest = withContext(Dispatchers.Default) {
		val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
		val derived = derive(password, salt, ITERATIONS)

		PasswordDigest(
			algorithm = ALGORITHM,
			iterations = ITERATIONS,
			salt = base64Encoder.encodeToString(salt),
			hash = base64Encoder.encodeToString(derived),
		)
	}

	override suspend fun verify(password: String, digest: PasswordDigest): Boolean =
		withContext(Dispatchers.Default) {
			if (digest.algorithm != ALGORITHM) return@withContext false

			val salt = runCatching { base64Decoder.decode(digest.salt) }.getOrNull()
			val expected = runCatching { base64Decoder.decode(digest.hash) }.getOrNull()
			if (salt == null || expected == null) return@withContext false

			// Usa os parâmetros gravados, não os atuais: digest antigo continua conferindo.
			val derived = derive(password, salt, digest.iterations)

			// Comparação em tempo constante: não denuncia quantos bytes bateram.
			MessageDigest.isEqual(derived, expected)
		}
}
