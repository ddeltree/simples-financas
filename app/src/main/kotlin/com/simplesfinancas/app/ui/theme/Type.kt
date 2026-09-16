package com.simplesfinancas.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.R

/** `--font-display: 'Fraunces'` — títulos e números. */
val DisplayFamily = FontFamily(
	Font(R.font.fraunces_medium, FontWeight.Medium),
	Font(R.font.fraunces_bold, FontWeight.Bold),
	Font(R.font.fraunces_bold, FontWeight.SemiBold),
)

/** `--font-sans: 'Manrope'` — todo o resto. */
val SansFamily = FontFamily(
	Font(R.font.manrope_regular, FontWeight.Normal),
	Font(R.font.manrope_medium, FontWeight.Medium),
	Font(R.font.manrope_semibold, FontWeight.SemiBold),
	Font(R.font.manrope_bold, FontWeight.Bold),
	Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

private val bodyBase = TextStyle(fontFamily = SansFamily)

val AppTypography = Typography(
	displayLarge = bodyBase.copy(fontFamily = DisplayFamily, fontSize = 40.sp, fontWeight = FontWeight.Bold),
	headlineLarge = bodyBase.copy(fontFamily = DisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Bold),
	headlineMedium = bodyBase.copy(fontFamily = DisplayFamily, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
	headlineSmall = bodyBase.copy(fontFamily = DisplayFamily, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
	titleLarge = bodyBase.copy(fontFamily = DisplayFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
	titleMedium = bodyBase.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
	bodyLarge = bodyBase.copy(fontSize = 16.sp),
	bodyMedium = bodyBase.copy(fontSize = 14.sp),
	bodySmall = bodyBase.copy(fontSize = 13.sp),
	labelLarge = bodyBase.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
	labelMedium = bodyBase.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
	labelSmall = bodyBase.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
)
