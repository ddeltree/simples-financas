package com.simplesfinancas.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A identidade da casa, portada de `styles.css` valor a valor. Material 3 não tem slot
 * para `ink`/`lagoon`/`palm`/`coral`, então a paleta viaja por um CompositionLocal e o
 * `ColorScheme` é derivado dela.
 */
@Immutable
data class IslandColors(
	val ink: Color,
	val inkSoft: Color,
	val lagoon: Color,
	val lagoonDeep: Color,
	val palm: Color,
	val palmSoft: Color,
	val coral: Color,
	val coralSoft: Color,
	val sand: Color,
	val foam: Color,
	val line: Color,
	val insetGlint: Color,
	val kicker: Color,
	val bgBase: Color,
	val surface: Color,
	val surfaceStrong: Color,
	val chipBg: Color,
	val chipLine: Color,
	val rowHover: Color,
	val heroA: Color,
	val heroB: Color,
	val heroC: Color,
	val scrim: Color,
	val onAccent: Color,
	val isDark: Boolean,
)

private val White = Color(0xFFFFFFFF)

val LightIslandColors = IslandColors(
	ink = Color(0xFF0F172A),
	inkSoft = Color(0xFF64748B),
	lagoon = Color(0xFF0F766E),
	lagoonDeep = Color(0xFF0F172A),
	palm = Color(0xFF16A34A),
	palmSoft = Color(0xFFDCFCE7),
	coral = Color(0xFFDC2626),
	coralSoft = Color(0xFFFEE2E2),
	sand = Color(0xFFF8FAFC),
	foam = Color(0xFFFFFFFF),
	line = Color(0xFFE2E8F0),
	insetGlint = White,
	kicker = Color(0xFF64748B),
	bgBase = Color(0xFFF8FAFC),
	surface = Color(0xFFFFFFFF),
	surfaceStrong = Color(0xFFFFFFFF),
	chipBg = Color(0xFFF1F5F9),
	chipLine = Color(0xFFE2E8F0),
	rowHover = Color(0xFFF8FAFC),
	heroA = Color(0xFFE2E8F0),
	heroB = Color(0xFFE2E8F0),
	heroC = Color(0xFFE2E8F0),
	scrim = Color(0x33000000),
	onAccent = White,
	isDark = false,
)

val DarkIslandColors = IslandColors(
	ink = Color(0xFFF8FAFC),
	inkSoft = Color(0xFF94A3B8),
	lagoon = Color(0xFF2DD4BF),
	lagoonDeep = Color(0xFFF8FAFC),
	palm = Color(0xFF22C55E),
	palmSoft = Color(0xFF22C55E).copy(alpha = 0.15f),
	coral = Color(0xFFEF4444),
	coralSoft = Color(0xFFEF4444).copy(alpha = 0.15f),
	sand = Color(0xFF0F172A),
	foam = Color(0xFF020617),
	line = Color(0xFF1E293B),
	insetGlint = Color(0xFF334155),
	kicker = Color(0xFF94A3B8),
	bgBase = Color(0xFF090D16),
	surface = Color(0xFF0F172A),
	surfaceStrong = Color(0xFF1E293B),
	chipBg = Color(0xFF1E293B),
	chipLine = Color(0xFF334155),
	rowHover = Color(0xFF1E293B),
	heroA = Color(0xFF1E293B),
	heroB = Color(0xFF1E293B),
	heroC = Color(0xFF1E293B),
	scrim = Color(0x80000000),
	onAccent = Color(0xFF0F172A),
	isDark = true,
)

val LocalIslandColors = staticCompositionLocalOf { LightIslandColors }
