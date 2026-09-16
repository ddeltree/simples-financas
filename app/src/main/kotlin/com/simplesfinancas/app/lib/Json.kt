package com.simplesfinancas.app.lib

import kotlinx.serialization.json.Json

/**
 * `explicitNulls = false` reproduz o `delete` do web: campo ausente, não `null`.
 * `ignoreUnknownKeys` deixa dados gravados por uma versão futura serem lidos sem quebrar.
 */
val AppJson: Json = Json {
	encodeDefaults = true
	explicitNulls = false
	ignoreUnknownKeys = true
	classDiscriminator = "type"
}
