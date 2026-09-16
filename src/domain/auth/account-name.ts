/**
 * Nome de conta é exibido como o usuário digitou, mas comparado sem diferenciar
 * maiúsculas nem espaços nas pontas — "Davi" e "davi " são a mesma conta.
 */
export function normalizeAccountName(name: string): string {
	return name.trim().toLocaleLowerCase("pt-BR");
}
