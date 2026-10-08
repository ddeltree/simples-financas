package com.simplesfinancas.app.domain

import com.simplesfinancas.app.lib.MonthKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Receita soma ao orçamento quando concluída; despesa subtrai. */
@Serializable
enum class TaskKind {
	@SerialName("income")
	INCOME,

	@SerialName("expense")
	EXPENSE,
}

@Serializable
sealed interface Recurrence {
	val dayOfMonth: Int?

	/** Repete todo mês, opcionalmente em um dia definido. */
	@Serializable
	@SerialName("monthly")
	data class Monthly(override val dayOfMonth: Int? = null) : Recurrence

	/** Acontece uma única vez no mês de startMonth, opcionalmente em um dia definido. */
	@Serializable
	@SerialName("once")
	data class Once(override val dayOfMonth: Int? = null) : Recurrence
}

/**
 * O que o usuário cadastra: o modelo da tarefa.
 * Nenhuma ocorrência é gravada em disco — elas são derivadas do modelo para o mês visitado.
 */
@Serializable
data class TaskTemplate(
	val id: String,
	val kind: TaskKind,
	val title: String,
	val amountCents: Long,
	val recurrence: Recurrence,
	val startMonth: MonthKey,
	val endMonth: MonthKey? = null,
	val createdAt: String,
)

/**
 * O que muda em UMA ocorrência específica (mês X do modelo Y).
 * Guardado por chave `${templateId}:${month}` — só existe quando o usuário mexeu nela.
 */
@Serializable
data class OccurrenceState(
	val done: Boolean = false,
	val doneAt: String? = null,
	/** Ajuste pontual: "a luz desse mês veio R$ 212,40". Ausente = segue o valor do modelo. */
	val amountCents: Long? = null,
)

@Serializable
data class FinanceState(
	val templates: List<TaskTemplate> = emptyList(),
	/** chave: `${templateId}:${month}` */
	val occurrences: Map<String, OccurrenceState> = emptyMap(),
)

/** Uma tarefa concreta, já resolvida para um mês. */
data class Occurrence(
	val id: String,
	val templateId: String,
	val month: MonthKey,
	val kind: TaskKind,
	val title: String,
	val amountCents: Long,
	val day: Int?,
	val date: String?,
	val done: Boolean,
	val doneAt: String?,
	val recurring: Boolean,
	/** true quando o valor deste mês difere do valor do modelo. */
	val adjusted: Boolean,
)

data class MonthSummary(
	val incomeDoneCents: Long,
	val incomePendingCents: Long,
	val incomeTotalCents: Long,
	val expenseDoneCents: Long,
	val expensePendingCents: Long,
	val expenseTotalCents: Long,
	/** O que de fato entrou menos o que de fato saiu — o saldo do bolso hoje. */
	val availableCents: Long,
	/** Projeção do fim do mês, contando o que ainda está pendente. */
	val projectedCents: Long,
	val tasksDone: Int,
	val tasksTotal: Int,
)

data class MonthView(
	val month: MonthKey,
	val income: List<Occurrence>,
	val expense: List<Occurrence>,
	val summary: MonthSummary,
)
