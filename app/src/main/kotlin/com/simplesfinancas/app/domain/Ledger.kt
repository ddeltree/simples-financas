package com.simplesfinancas.app.domain

import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.PT_BR
import com.simplesfinancas.app.lib.clampDay
import com.simplesfinancas.app.lib.isoDate
import java.text.Collator

fun occurrenceKey(templateId: String, month: MonthKey): String = "$templateId:$month"

/** Modelos "once" valem só no mês de origem; "monthly" vale da origem até o fim (se houver). */
fun occursIn(template: TaskTemplate, month: MonthKey): Boolean {
	if (template.recurrence is Recurrence.Once) return template.startMonth == month
	if (month < template.startMonth) return false
	if (template.endMonth != null && month > template.endMonth) return false
	return true
}

private fun toOccurrence(
	template: TaskTemplate,
	month: MonthKey,
	state: FinanceState,
): Occurrence {
	val stored = state.occurrences[occurrenceKey(template.id, month)]
	val amountCents = stored?.amountCents ?: template.amountCents
	val day = clampDay(month, template.recurrence.dayOfMonth)

	return Occurrence(
		id = occurrenceKey(template.id, month),
		templateId = template.id,
		month = month,
		kind = template.kind,
		title = template.title,
		amountCents = amountCents,
		day = day,
		date = isoDate(month, day),
		done = stored?.done ?: false,
		doneAt = stored?.doneAt,
		recurring = template.recurrence is Recurrence.Monthly,
		adjusted = amountCents != template.amountCents,
	)
}

private val titleCollator: Collator = Collator.getInstance(PT_BR)

private val byDayThenTitle: Comparator<Occurrence> =
	compareBy<Occurrence> { it.day }.thenBy(titleCollator) { it.title }

private fun sumCents(occurrences: List<Occurrence>): Long =
	occurrences.sumOf { it.amountCents }

fun summarize(income: List<Occurrence>, expense: List<Occurrence>): MonthSummary {
	val incomeDone = income.filter { it.done }
	val expenseDone = expense.filter { it.done }

	val incomeDoneCents = sumCents(incomeDone)
	val incomeTotalCents = sumCents(income)
	val expenseDoneCents = sumCents(expenseDone)
	val expenseTotalCents = sumCents(expense)

	return MonthSummary(
		incomeDoneCents = incomeDoneCents,
		incomePendingCents = incomeTotalCents - incomeDoneCents,
		incomeTotalCents = incomeTotalCents,
		expenseDoneCents = expenseDoneCents,
		expensePendingCents = expenseTotalCents - expenseDoneCents,
		expenseTotalCents = expenseTotalCents,
		availableCents = incomeDoneCents - expenseDoneCents,
		projectedCents = incomeTotalCents - expenseTotalCents,
		tasksDone = incomeDone.size + expenseDone.size,
		tasksTotal = income.size + expense.size,
	)
}

/** Ponto único de leitura da tela: dos modelos guardados para as tarefas de um mês. */
fun buildMonthView(state: FinanceState, month: MonthKey): MonthView {
	val occurrences = state.templates
		.filter { occursIn(it, month) }
		.map { toOccurrence(it, month, state) }

	val income = occurrences.filter { it.kind == TaskKind.INCOME }.sortedWith(byDayThenTitle)
	val expense = occurrences.filter { it.kind == TaskKind.EXPENSE }.sortedWith(byDayThenTitle)

	return MonthView(month, income, expense, summarize(income, expense))
}
