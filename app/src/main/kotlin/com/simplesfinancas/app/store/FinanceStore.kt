package com.simplesfinancas.app.store

import com.simplesfinancas.app.domain.FinanceState
import com.simplesfinancas.app.domain.OccurrenceState
import com.simplesfinancas.app.domain.Recurrence
import com.simplesfinancas.app.domain.TaskKind
import com.simplesfinancas.app.domain.TaskTemplate
import com.simplesfinancas.app.domain.occurrenceKey
import com.simplesfinancas.app.lib.AppJson
import com.simplesfinancas.app.lib.KeyValueStore
import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.currentMonthKey
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val STORAGE_PREFIX = "simples-financas:v1"

/** Chave usada antes de existirem contas — adotada pelo primeiro usuário que entrar. */
private const val LEGACY_STORAGE_KEY = STORAGE_PREFIX

private val EMPTY = FinanceState()

private fun newId(): String = UUID.randomUUID().toString()

private fun nowIso(): String = Instant.now().toString()

data class TemplateInput(
	val kind: TaskKind,
	val title: String,
	val amountCents: Long,
	val recurrence: Recurrence,
	val startMonth: MonthKey,
)

/**
 * O estado financeiro do usuário logado. Não conhece autenticação: quem aponta a store
 * para a gaveta de alguém é a ponte de sessão, via [setStorageScope].
 */
class FinanceStore(
	private val storage: KeyValueStore,
	scope: CoroutineScope,
) {
	private val _state = MutableStateFlow(EMPTY)
	val state: StateFlow<FinanceState> = _state.asStateFlow()

	/** Usuário dono dos dados em memória. `null` = ninguém logado, nada a ler nem gravar. */
	private var storageScope: String? = null

	private class Write(val key: String, val json: String)

	// Fila serial: garante que a ordem das gravações é a ordem das ações.
	private val writes = Channel<Write>(Channel.UNLIMITED)

	init {
		scope.launch {
			for (write in writes) storage.write(write.key, write.json)
		}
	}

	private fun storageKey(): String? =
		storageScope?.let { "$STORAGE_PREFIX:u:$it" }

	private fun parseState(raw: String?): FinanceState? {
		if (raw.isNullOrEmpty()) return null
		return runCatching { AppJson.decodeFromString<FinanceState>(raw) }.getOrNull()
	}

	private suspend fun readStorage(): FinanceState {
		val key = storageKey() ?: return EMPTY
		return parseState(storage.read(key)) ?: EMPTY
	}

	/**
	 * Dados do protótipo pré-contas: o primeiro usuário que abrir uma gaveta vazia herda o
	 * que estava na chave antiga. Como a chave é removida em seguida, só acontece uma vez.
	 */
	private suspend fun adoptLegacyState(key: String): FinanceState? {
		if (storage.read(key) != null) return null

		val legacy = parseState(storage.read(LEGACY_STORAGE_KEY)) ?: return null

		storage.write(key, AppJson.encodeToString(legacy))
		storage.remove(LEGACY_STORAGE_KEY)
		return legacy
	}

	/**
	 * Aponta a store para a gaveta de um usuário (ou para lugar nenhum, no logout). Quem
	 * chama é a ponte de sessão — esta store não conhece autenticação.
	 */
	suspend fun setStorageScope(next: String?) {
		if (storageScope == next) return

		storageScope = next

		val key = storageKey()
		_state.value = if (key == null) EMPTY else adoptLegacyState(key) ?: readStorage()
	}

	private fun setState(next: FinanceState) {
		_state.value = next
		val key = storageKey() ?: return
		writes.trySend(Write(key, AppJson.encodeToString(next)))
	}

	fun addTemplate(input: TemplateInput) {
		val template = TaskTemplate(
			id = newId(),
			kind = input.kind,
			title = input.title.trim(),
			amountCents = input.amountCents,
			recurrence = input.recurrence,
			startMonth = input.startMonth,
			endMonth = null,
			createdAt = nowIso(),
		)

		setState(_state.value.let { it.copy(templates = it.templates + template) })
	}

	/** O patch do web: só os campos informados mudam. */
	fun updateTemplate(
		id: String,
		kind: TaskKind? = null,
		title: String? = null,
		amountCents: Long? = null,
		recurrence: Recurrence? = null,
		startMonth: MonthKey? = null,
		endMonth: MonthKey? = null,
		clearEndMonth: Boolean = false,
	) {
		val current = _state.value
		setState(
			current.copy(
				templates = current.templates.map { template ->
					if (template.id != id) {
						template
					} else {
						template.copy(
							kind = kind ?: template.kind,
							title = title ?: template.title,
							amountCents = amountCents ?: template.amountCents,
							recurrence = recurrence ?: template.recurrence,
							startMonth = startMonth ?: template.startMonth,
							endMonth = if (clearEndMonth) null else endMonth ?: template.endMonth,
						)
					}
				},
			),
		)
	}

	fun deleteTemplate(id: String) {
		val current = _state.value
		setState(
			FinanceState(
				templates = current.templates.filter { it.id != id },
				occurrences = current.occurrences.filterKeys { !it.startsWith("$id:") },
			),
		)
	}

	/** Encerra um modelo recorrente a partir do mês seguinte, preservando o histórico já concluído. */
	fun endTemplateAfter(id: String, month: MonthKey) {
		updateTemplate(id, endMonth = month)
	}

	private fun patchOccurrence(
		templateId: String,
		month: MonthKey,
		patch: (OccurrenceState) -> OccurrenceState,
	) {
		val key = occurrenceKey(templateId, month)
		val current = _state.value
		val occurrence = current.occurrences[key] ?: OccurrenceState()

		setState(current.copy(occurrences = current.occurrences + (key to patch(occurrence))))
	}

	fun setOccurrenceDone(templateId: String, month: MonthKey, done: Boolean) {
		patchOccurrence(templateId, month) {
			it.copy(done = done, doneAt = if (done) nowIso() else null)
		}
	}

	/** Ajuste só deste mês; `null` volta a seguir o valor do modelo. */
	fun setOccurrenceAmount(templateId: String, month: MonthKey, amountCents: Long?) {
		patchOccurrence(templateId, month) { it.copy(amountCents = amountCents) }
	}

	fun resetAll() {
		setState(EMPTY)
	}

	/** Dados de demonstração para quem abre o app pela primeira vez. */
	fun seedExamples() {
		val startMonth = currentMonthKey()
		val createdAt = nowIso()

		fun make(kind: TaskKind, title: String, amount: Long, dayOfMonth: Int) = TaskTemplate(
			id = newId(),
			kind = kind,
			title = title,
			amountCents = amount,
			recurrence = Recurrence.Monthly(dayOfMonth),
			startMonth = startMonth,
			endMonth = null,
			createdAt = createdAt,
		)

		setState(
			FinanceState(
				occurrences = emptyMap(),
				templates = listOf(
					make(TaskKind.INCOME, "Salário", 480000, 5),
					make(TaskKind.INCOME, "Freela", 60000, 20),
					make(TaskKind.EXPENSE, "Aluguel", 150000, 10),
					make(TaskKind.EXPENSE, "Cartão de crédito", 120000, 14),
					make(TaskKind.EXPENSE, "Mercado", 90000, 8),
					make(TaskKind.EXPENSE, "Energia", 18000, 12),
					make(TaskKind.EXPENSE, "Internet", 12000, 15),
					make(TaskKind.EXPENSE, "Academia", 11000, 3),
				),
			),
		)
	}
}
