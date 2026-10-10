package com.simplesfinancas.app.domain.auth

import com.simplesfinancas.app.lib.PT_BR

/**
 * Nome de conta é exibido como o usuário digitou, mas comparado sem diferenciar
 * maiúsculas nem espaços nas pontas — "Davi" e "davi " são a mesma conta.
 */
fun normalizeAccountName(name: String): String = name.trim().lowercase(PT_BR)
