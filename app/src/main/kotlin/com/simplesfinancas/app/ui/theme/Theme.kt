package com.simplesfinancas.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun SimplesFinancasTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	val colors = if (darkTheme) DarkIslandColors else LightIslandColors

	// O Material só precisa dos slots que a UI de fato usa (ripple, cursor, seleção);
	// a identidade visual vem de LocalIslandColors.
	val scheme = if (darkTheme) {
		darkColorScheme(
			primary = colors.lagoonDeep,
			onPrimary = colors.onAccent,
			secondary = colors.palm,
			background = colors.bgBase,
			onBackground = colors.ink,
			surface = colors.foam,
			onSurface = colors.ink,
			onSurfaceVariant = colors.inkSoft,
			error = colors.coral,
			outline = colors.line,
		)
	} else {
		lightColorScheme(
			primary = colors.lagoonDeep,
			onPrimary = colors.onAccent,
			secondary = colors.palm,
			background = colors.bgBase,
			onBackground = colors.ink,
			surface = colors.foam,
			onSurface = colors.ink,
			onSurfaceVariant = colors.inkSoft,
			error = colors.coral,
			outline = colors.line,
		)
	}

	CompositionLocalProvider(LocalIslandColors provides colors) {
		MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
	}
}

/** Atalho para a paleta da casa, do jeito que a UI lê. */
val islandColors: IslandColors
	@Composable get() = LocalIslandColors.current
