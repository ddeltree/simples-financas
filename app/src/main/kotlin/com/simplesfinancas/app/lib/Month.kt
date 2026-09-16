package com.simplesfinancas.app.lib

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mês no formato "YYYY-MM". Ordenação lexicográfica == ordenação cronológica. */
typealias MonthKey = String

val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

private val MONTH_LABEL = DateTimeFormatter.ofPattern("LLLL 'de' yyyy", PT_BR)

private val MONTH_LABEL_SHORT = DateTimeFormatter.ofPattern("LLL", PT_BR)

private fun pad(value: Int): String = value.toString().padStart(2, '0')

fun monthKeyOf(date: LocalDate): MonthKey = "${date.year}-${pad(date.monthValue)}"

fun currentMonthKey(): MonthKey = monthKeyOf(LocalDate.now())

fun parseMonthKey(month: MonthKey): YearMonth {
	val (year, monthNumber) = month.split("-")
	return YearMonth.of(year.toInt(), monthNumber.toInt())
}

private fun monthKeyOf(yearMonth: YearMonth): MonthKey =
	"${yearMonth.year}-${pad(yearMonth.monthValue)}"

fun addMonths(month: MonthKey, delta: Long): MonthKey =
	monthKeyOf(parseMonthKey(month).plusMonths(delta))

fun daysInMonth(month: MonthKey): Int = parseMonthKey(month).lengthOfMonth()

/** "Todo dia 31" em fevereiro vira dia 28/29 — a ocorrência nunca some do mês. */
fun clampDay(month: MonthKey, day: Int): Int = day.coerceIn(1, daysInMonth(month))

fun isoDate(month: MonthKey, day: Int): String = "$month-${pad(clampDay(month, day))}"

fun monthOfIsoDate(date: String): MonthKey = date.take(7)

fun formatMonthLabel(month: MonthKey): String {
	val label = parseMonthKey(month).atDay(1).format(MONTH_LABEL)
	return label.replaceFirstChar { it.titlecase(PT_BR) }
}

fun formatMonthShort(month: MonthKey): String =
	parseMonthKey(month).atDay(1).format(MONTH_LABEL_SHORT).replace(".", "")

fun todayIso(): String = LocalDate.now().let { "${monthKeyOf(it)}-${pad(it.dayOfMonth)}" }
