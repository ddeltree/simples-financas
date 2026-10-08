package com.simplesfinancas.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.domain.MonthSummary
import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.formatBRL
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.islandColors
import kotlin.math.abs

@Composable
fun BudgetSummary(month: MonthKey, summary: MonthSummary, modifier: Modifier = Modifier) {
	val colors = islandColors
	val montante = summary.availableCents

	val formattedMontante = when {
		montante > 0 -> "+ ${formatBRL(montante)}"
		montante < 0 -> "− ${formatBRL(abs(montante))}"
		else -> formatBRL(0L)
	}

	val montanteColor = when {
		montante > 0 -> colors.palm
		montante < 0 -> colors.coral
		else -> colors.ink
	}

	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(vertical = 12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = formattedMontante,
			color = montanteColor,
			fontFamily = DisplayFamily,
			fontWeight = FontWeight.Bold,
			fontSize = 42.sp,
			textAlign = TextAlign.Center,
		)
	}
}
