package com.simplesfinancas.app

import com.simplesfinancas.app.auth.Pbkdf2PasswordHasher
import com.simplesfinancas.app.domain.FinanceState
import com.simplesfinancas.app.domain.OccurrenceState
import com.simplesfinancas.app.domain.Recurrence
import com.simplesfinancas.app.domain.TaskKind
import com.simplesfinancas.app.domain.TaskTemplate
import com.simplesfinancas.app.domain.buildMonthView
import com.simplesfinancas.app.domain.occurrenceKey
import com.simplesfinancas.app.lib.AppJson
import com.simplesfinancas.app.lib.centsToInputValue
import com.simplesfinancas.app.lib.clampDay
import com.simplesfinancas.app.lib.formatBRL
import com.simplesfinancas.app.lib.formatMonthLabel
import com.simplesfinancas.app.lib.parseAmountToCents
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Paridade com a versão web: os casos que o port precisava manter idênticos.
 * Não é TDD — foi escrito depois, para provar que a tradução não mudou a regra.
 */
class ParityTest {

	@Test
	fun `dinheiro entra em centavos independentemente do formato digitado`() {
		assertEquals(180000L, parseAmountToCents("1.800"))
		assertEquals(9990L, parseAmountToCents("99.9"))
		assertEquals(123456L, parseAmountToCents("1.234,56"))
		assertEquals(123456L, parseAmountToCents("1234,56"))
		assertEquals(123456L, parseAmountToCents("1234.56"))
		assertEquals(123400L, parseAmountToCents("1234"))
		assertEquals(21240L, parseAmountToCents("R$ 212,40"))
		assertNull(parseAmountToCents(""))
		assertNull(parseAmountToCents("abc"))
	}

	@Test
	fun `dinheiro sai sempre por formatBRL`() {
		// O separador de moeda do ICU é espaço não separável; normaliza antes de comparar.
		fun plain(value: String) = value.replace('\u00A0', ' ').replace('\u202F', ' ')

		assertEquals("R$ 42,50", plain(formatBRL(4250)))
		assertEquals("-R$ 42,50", plain(formatBRL(-4250)))
		assertEquals("R$ 0,00", plain(formatBRL(0)))
		assertEquals("1234,56", centsToInputValue(123456))
	}

	@Test
	fun `todo dia 31 em fevereiro vira o ultimo dia do mes`() {
		assertEquals(29, clampDay("2024-02", 31))
		assertEquals(28, clampDay("2026-02", 31))
		assertEquals(1, clampDay("2026-02", 0))
		assertEquals(31, clampDay("2026-01", 31))
	}

	@Test
	fun `rotulo do mes sai em pt-BR com inicial maiuscula`() {
		assertEquals("Setembro de 2026", formatMonthLabel("2026-09"))
	}

	private fun template(
		id: String,
		kind: TaskKind = TaskKind.EXPENSE,
		title: String = "Aluguel",
		amount: Long = 150000,
		recurrence: Recurrence = Recurrence.Monthly(10),
		startMonth: String = "2026-01",
		endMonth: String? = null,
	) = TaskTemplate(
		id = id,
		kind = kind,
		title = title,
		amountCents = amount,
		recurrence = recurrence,
		startMonth = startMonth,
		endMonth = endMonth,
		createdAt = "2026-01-01T00:00:00Z",
	)

	@Test
	fun `recorrente aparece em meses futuros sem nada gravado`() {
		val state = FinanceState(templates = listOf(template("t1")))

		val view = buildMonthView(state, "2026-09")

		assertEquals(1, view.expense.size)
		assertEquals("2026-09-10", view.expense[0].date)
		assertFalse(view.expense[0].done)
		assertEquals(150000L, view.summary.expenseTotalCents)
		assertEquals(0L, view.summary.expenseDoneCents)
	}

	@Test
	fun `endMonth encerra o recorrente preservando o historico`() {
		val state = FinanceState(templates = listOf(template("t1", endMonth = "2026-03")))

		assertEquals(1, buildMonthView(state, "2026-03").expense.size)
		assertEquals(0, buildMonthView(state, "2026-04").expense.size)
	}

	@Test
	fun `once so existe no mes de origem`() {
		val state = FinanceState(
			templates = listOf(
				template("t1", recurrence = Recurrence.Once(5), startMonth = "2026-02"),
			),
		)

		assertEquals(1, buildMonthView(state, "2026-02").expense.size)
		assertEquals(0, buildMonthView(state, "2026-03").expense.size)
	}

	@Test
	fun `ajuste pontual vale so no mes ajustado`() {
		val state = FinanceState(
			templates = listOf(template("t1", title = "Energia", amount = 18000)),
			occurrences = mapOf(
				occurrenceKey("t1", "2026-05") to OccurrenceState(amountCents = 21240),
			),
		)

		val ajustado = buildMonthView(state, "2026-05").expense[0]
		assertEquals(21240L, ajustado.amountCents)
		assertTrue(ajustado.adjusted)

		val seguinte = buildMonthView(state, "2026-06").expense[0]
		assertEquals(18000L, seguinte.amountCents)
		assertFalse(seguinte.adjusted)
	}

	@Test
	fun `orcamento conta so o que foi concluido no disponivel`() {
		val state = FinanceState(
			templates = listOf(
				template("i1", kind = TaskKind.INCOME, title = "Salário", amount = 480000),
				template("e1", amount = 150000),
			),
			occurrences = mapOf(
				occurrenceKey("i1", "2026-04") to OccurrenceState(done = true),
			),
		)

		val summary = buildMonthView(state, "2026-04").summary
		assertEquals(480000L, summary.availableCents)
		assertEquals(330000L, summary.projectedCents)
		assertEquals(1, summary.tasksDone)
		assertEquals(2, summary.tasksTotal)
	}

	@Test
	fun `json gravado mantem o formato do localStorage`() {
		val state = FinanceState(
			templates = listOf(template("t1")),
			occurrences = mapOf("t1:2026-01" to OccurrenceState(done = true, doneAt = null)),
		)

		val json = AppJson.encodeToString(state)

		assertTrue(json.contains("\"type\":\"monthly\""))
		assertTrue(json.contains("\"kind\":\"expense\""))
		// `explicitNulls = false` reproduz o `delete` do web: sem ajuste, sem campo.
		assertFalse(json.contains("amountCents\":null"))
		assertEquals(state, AppJson.decodeFromString<FinanceState>(json))
	}

	@Test
	fun `senha confere pelo digest e recusa a errada`() = runBlocking {
		val hasher = Pbkdf2PasswordHasher()
		val digest = hasher.hash("segredo123")

		assertEquals("PBKDF2-SHA256", digest.algorithm)
		assertEquals(210_000, digest.iterations)
		assertTrue(hasher.verify("segredo123", digest))
		assertFalse(hasher.verify("segredo124", digest))
	}
}
