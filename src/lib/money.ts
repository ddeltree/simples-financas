const BRL = new Intl.NumberFormat("pt-BR", {
	style: "currency",
	currency: "BRL",
});

/** Valores circulam em centavos (inteiro) para não acumular erro de ponto flutuante. */
export function formatBRL(cents: number): string {
	return BRL.format(cents / 100);
}

/** Aceita "1.234,56", "1234,56", "1234.56" e "1234". Devolve null se não for um valor legível. */
export function parseAmountToCents(input: string): number | null {
	const cleaned = input.replace(/[^\d,.-]/g, "").trim();
	if (!cleaned) return null;

	// Sem vírgula, "1.800" é milhar pt-BR e "99.9" é decimal — o padrão de grupos decide.
	const thousandsOnly = /^-?\d{1,3}(\.\d{3})+$/.test(cleaned);
	const normalized = cleaned.includes(",")
		? cleaned.replace(/\./g, "").replace(",", ".")
		: thousandsOnly
			? cleaned.replace(/\./g, "")
			: cleaned;

	const value = Number(normalized);
	if (!Number.isFinite(value)) return null;

	return Math.round(value * 100);
}

/** Formato do input de edição: "1234,56" (sem separador de milhar, para não atrapalhar a digitação). */
export function centsToInputValue(cents: number): string {
	return (cents / 100).toFixed(2).replace(".", ",");
}
