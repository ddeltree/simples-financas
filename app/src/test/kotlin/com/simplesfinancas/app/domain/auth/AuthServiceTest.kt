package com.simplesfinancas.app.domain.auth

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O serviço de autenticação rodando sem Android nenhum: nem PBKDF2, nem DataStore, nem
 * relógio do sistema. Só é possível porque [AuthService] depende das portas de
 * [Ports.kt] e não das classes concretas — aqui elas entram como dublês em memória.
 */
class AuthServiceTest {

	/** Hash de mentira: barato e reversível, para o teste não gastar 210 000 iterações. */
	private class FakeHasher : PasswordHasher {
		override suspend fun hash(password: String) = PasswordDigest(
			algorithm = "fake",
			iterations = 1,
			salt = "sal",
			hash = "sal:$password",
		)

		override suspend fun verify(password: String, digest: PasswordDigest) =
			digest.hash == "sal:$password"
	}

	/** Implementa as duas portas de conta, como o repositório de verdade. */
	private class FakeAccounts : AccountReader, AccountWriter {
		val rows = mutableListOf<StoredAccount>()

		override suspend fun findByName(name: String): StoredAccount? {
			val wanted = normalizeAccountName(name)
			return rows.find { normalizeAccountName(it.name) == wanted }
		}

		override suspend fun findById(id: String) = rows.find { it.id == id }

		override suspend fun list() = rows.map { it.toSummary() }

		override suspend fun create(account: StoredAccount) {
			rows += account
		}

		override suspend fun update(account: StoredAccount) {
			rows.replaceAll { if (it.id == account.id) account else it }
		}
	}

	private class FakeSessions : SessionStore {
		var record: SessionRecord? = null

		override suspend fun read() = record

		override suspend fun write(record: SessionRecord) {
			this.record = record
		}

		override suspend fun clear() {
			record = null
		}
	}

	/** O relógio é porta justamente para isto: expirar uma sessão sem esperar 12 horas. */
	private class FakeClock(var millis: Long = 1_700_000_000_000) : Clock {
		override fun now() = millis
	}

	private class SequentialIds : IdGenerator {
		private var next = 0

		override fun newId() = "id-${++next}"
	}

	private class Fixture {
		val accounts = FakeAccounts()
		val sessions = FakeSessions()
		val clock = FakeClock()
		val service = AuthService(
			hasher = FakeHasher(),
			accountReader = accounts,
			accountWriter = accounts,
			sessions = sessions,
			clock = clock,
			ids = SequentialIds(),
		)
	}

	private fun credentials(
		name: String = "Davi",
		password: String = "segredo123",
		remember: Boolean = false,
	) = CredentialsInput(name = name, password = password, remember = remember)

	private fun <T> ok(result: AuthResult<T>): T {
		assertTrue("esperava sucesso, veio $result", result is AuthResult.Ok)
		return (result as AuthResult.Ok).value
	}

	private fun err(result: AuthResult<*>): AuthErrorCode {
		assertTrue("esperava erro, veio $result", result is AuthResult.Err)
		return (result as AuthResult.Err).error.code
	}

	@Test
	fun `criar conta guarda o digest, nunca a senha em texto`() = runBlocking {
		val f = Fixture()

		val account = ok(f.service.register(credentials()))

		assertEquals("Davi", account.name)
		val stored = f.accounts.rows.single()
		assertEquals("sal:segredo123", stored.password.hash)
		assertTrue(f.accounts.rows.none { it.password.hash == "segredo123" })
	}

	@Test
	fun `criar conta ja deixa a sessao aberta`() = runBlocking {
		val f = Fixture()

		val account = ok(f.service.register(credentials()))

		val session = requireNotNull(f.sessions.record) { "sessão deveria existir" }
		assertEquals(account.id, session.userId)
		assertEquals(f.clock.millis + SESSION_TTL_MS, session.expiresAt)
	}

	@Test
	fun `manter conectado estende a validade da sessao`() = runBlocking {
		val f = Fixture()

		f.service.register(credentials(remember = true))

		assertEquals(f.clock.millis + REMEMBER_TTL_MS, f.sessions.record?.expiresAt)
	}

	@Test
	fun `nome vazio e senha curta sao recusados antes de gravar`() = runBlocking {
		val f = Fixture()

		assertEquals(AuthErrorCode.NOME_OBRIGATORIO, err(f.service.register(credentials(name = "   "))))
		assertEquals(AuthErrorCode.SENHA_CURTA, err(f.service.register(credentials(password = "123"))))
		assertTrue(f.accounts.rows.isEmpty())
		assertNull(f.sessions.record)
	}

	@Test
	fun `nome em uso ignora maiusculas e espacos nas pontas`() = runBlocking {
		val f = Fixture()
		f.service.register(credentials(name = "Davi"))

		assertEquals(AuthErrorCode.NOME_EM_USO, err(f.service.register(credentials(name = " davi "))))
		assertEquals(1, f.accounts.rows.size)
	}

	@Test
	fun `entrar confere a senha pelo digest`() = runBlocking {
		val f = Fixture()
		val created = ok(f.service.register(credentials()))
		f.service.signOut()

		val signedIn = ok(f.service.signIn(credentials()))

		assertEquals(created.id, signedIn.id)
		assertEquals(created.id, f.sessions.record?.userId)
	}

	@Test
	fun `senha errada e conta inexistente dao o mesmo erro`() = runBlocking {
		val f = Fixture()
		f.service.register(credentials())
		f.service.signOut()

		assertEquals(
			AuthErrorCode.CREDENCIAIS_INVALIDAS,
			err(f.service.signIn(credentials(password = "outra-coisa"))),
		)
		assertEquals(
			AuthErrorCode.CREDENCIAIS_INVALIDAS,
			err(f.service.signIn(credentials(name = "Ninguém"))),
		)
		assertNull(f.sessions.record)
	}

	@Test
	fun `sair apaga a sessao mas nao a conta`() = runBlocking {
		val f = Fixture()
		f.service.register(credentials())

		f.service.signOut()

		assertNull(f.sessions.record)
		assertEquals(1, f.accounts.rows.size)
	}

	@Test
	fun `restaurar devolve quem esta logado`() = runBlocking {
		val f = Fixture()
		val created = ok(f.service.register(credentials()))

		assertEquals(created.id, f.service.restore()?.id)
	}

	@Test
	fun `sessao vencida nao restaura e some do armazenamento`() = runBlocking {
		val f = Fixture()
		f.service.register(credentials())

		f.clock.millis += SESSION_TTL_MS

		assertNull(f.service.restore())
		assertNull(f.sessions.record)
	}

	@Test
	fun `sessao de conta que sumiu tambem e descartada`() = runBlocking {
		val f = Fixture()
		f.service.register(credentials())
		f.accounts.rows.clear()

		assertNull(f.service.restore())
		assertNull(f.sessions.record)
	}

	@Test
	fun `listar contas nunca expoe o digest`() = runBlocking {
		val f = Fixture()
		f.service.register(credentials(name = "Davi"))
		f.service.register(credentials(name = "Outra"))

		val names = f.service.listAccounts().map { it.name }

		assertEquals(listOf("Davi", "Outra"), names)
	}
}
