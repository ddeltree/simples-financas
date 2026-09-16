package com.simplesfinancas.app.lib

import java.math.BigDecimal
import java.text.NumberFormat

private val BRL: NumberFormat = NumberFormat.getCurrencyInstance(PT_BR)

private fun reais(cents: Long): BigDecimal = BigDecimal.valueOf(cents).movePointLeft(2)

/** Valores circulam em centavos (inteiro) para não acumular erro de ponto flutuante. */
fun formatBRL(cents: Long): String = BRL.format(reais(cents))

private val ALLOWED = Regex("[^\\d,.-]")
private val THOUSANDS_ONLY = Regex("^-?\\d{1,3}(\\.\\d{3})+$")

/** Aceita "1.234,56", "1234,56", "1234.56" e "1234". Devolve null se não for um valor legível. */
fun parseAmountToCents(input: String): Long? {
	val cleaned = input.replace(ALLOWED, "").trim()
	if (cleaned.isEmpty()) return null

	// Sem vírgula, "1.800" é milhar pt-BR e "99.9" é decimal — o padrão de grupos decide.
	val normalized = when {
		cleaned.contains(",") -> cleaned.replace(".", "").replace(",", ".")
		THOUSANDS_ONLY.matches(cleaned) -> cleaned.replace(".", "")
		else -> cleaned
	}

	val value = normalized.toDoubleOrNull() ?: return null
	if (!value.isFinite()) return null

	return Math.round(value * 100)
}

/** Formato do input de edição: "1234,56" (sem separador de milhar, para não atrapalhar a digitação). */
fun centsToInputValue(cents: Long): String =
	reais(cents).setScale(2).toPlainString().replace(".", ",")
