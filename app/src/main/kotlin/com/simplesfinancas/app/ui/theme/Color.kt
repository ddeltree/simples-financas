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
	ink = Color(0xFF173A40),
	inkSoft = Color(0xFF416166),
	lagoon = Color(0xFF4FB8B2),
	lagoonDeep = Color(0xFF328F97),
	palm = Color(0xFF2F6A4A),
	palmSoft = Color(0xFF2F6A4A).copy(alpha = 0.12f),
	coral = Color(0xFFBD4F3C),
	coralSoft = Color(0xFFBD4F3C).copy(alpha = 0.12f),
	sand = Color(0xFFE7F0E8),
	foam = Color(0xFFF3FAF5),
	line = Color(0xFF173A40).copy(alpha = 0.14f),
	insetGlint = White.copy(alpha = 0.82f),
	kicker = Color(0xFF2F6A4A).copy(alpha = 0.9f),
	bgBase = Color(0xFFE7F3EC),
	surface = White.copy(alpha = 0.74f),
	surfaceStrong = White.copy(alpha = 0.9f),
	chipBg = White.copy(alpha = 0.8f),
	chipLine = Color(0xFF2F6A4A).copy(alpha = 0.18f),
	rowHover = White.copy(alpha = 0.9f),
	heroA = Color(0xFF4FB8B2).copy(alpha = 0.36f),
	heroB = Color(0xFF2F6A4A).copy(alpha = 0.2f),
	heroC = Color(0xFF4FB8B2).copy(alpha = 0.1f),
	scrim = Color(0xFF173A40).copy(alpha = 0.38f),
	onAccent = White,
	isDark = false,
)

val DarkIslandColors = IslandColors(
	ink = Color(0xFFD7ECE8),
	inkSoft = Color(0xFFAFCDC8),
	lagoon = Color(0xFF60D7CF),
	lagoonDeep = Color(0xFF8DE5DB),
	palm = Color(0xFF6EC89A),
	palmSoft = Color(0xFF6EC89A).copy(alpha = 0.16f),
	coral = Color(0xFFF08B76),
	coralSoft = Color(0xFFF08B76).copy(alpha = 0.16f),
	sand = Color(0xFF0F1A1E),
	foam = Color(0xFF101D22),
	line = Color(0xFF8DE5DB).copy(alpha = 0.18f),
	insetGlint = Color(0xFFC2F7EE).copy(alpha = 0.14f),
	kicker = Color(0xFFB8EFE5),
	bgBase = Color(0xFF0A1418),
	surface = Color(0xFF101E22).copy(alpha = 0.8f),
	surfaceStrong = Color(0xFF0F1B1F).copy(alpha = 0.92f),
	chipBg = Color(0xFF0D1C20).copy(alpha = 0.9f),
	chipLine = Color(0xFF8DE5DB).copy(alpha = 0.24f),
	rowHover = Color(0xFF182C31).copy(alpha = 0.8f),
	heroA = Color(0xFF60D7CF).copy(alpha = 0.18f),
	heroB = Color(0xFF6EC89A).copy(alpha = 0.12f),
	heroC = Color(0xFF60D7CF).copy(alpha = 0.08f),
	scrim = Color(0xFF020A0C).copy(alpha = 0.55f),
	onAccent = Color(0xFF07141A),
	isDark = true,
)

val LocalIslandColors = staticCompositionLocalOf { LightIslandColors }
