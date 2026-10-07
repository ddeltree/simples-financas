package com.simplesfinancas.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.MonthSummary
import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.formatBRL
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell

@Composable
fun BudgetSummary(month: MonthKey, summary: MonthSummary, modifier: Modifier = Modifier) {
	val colors = islandColors
	val montante = summary.incomeTotalCents - summary.expenseTotalCents

	Column(
		modifier = modifier
			.fillMaxWidth()
			.islandShell(colors)
			.padding(horizontal = 20.dp, vertical = 20.dp),
	) {
		Text(
			text = stringResource(R.string.montante_label),
			color = colors.inkSoft,
			fontFamily = SansFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 13.sp,
		)

		Text(
			text = formatBRL(montante),
			modifier = Modifier.padding(top = 4.dp),
			color = if (montante < 0) colors.coral else colors.ink,
			fontFamily = DisplayFamily,
			fontWeight = FontWeight.Bold,
			fontSize = 32.sp,
		)
	}
}
