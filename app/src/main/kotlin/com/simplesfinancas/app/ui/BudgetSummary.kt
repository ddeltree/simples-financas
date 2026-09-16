package com.simplesfinancas.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.MonthSummary
import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.PT_BR
import com.simplesfinancas.app.lib.formatBRL
import com.simplesfinancas.app.lib.formatMonthLabel
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.IslandKicker
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.chipSurface
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell
import kotlin.math.roundToInt

private enum class Tone { INCOME, EXPENSE, NEUTRAL }

@Composable
private fun Stat(label: String, value: String, tone: Tone, modifier: Modifier = Modifier) {
	val colors = islandColors
	Column(
		modifier = modifier
			.chipSurface(colors, corner = 16.dp)
			.padding(horizontal = 16.dp, vertical = 12.dp),
	) {
		Text(
			text = label.uppercase(),
			color = colors.inkSoft,
			fontFamily = SansFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 11.sp,
			letterSpacing = 1.32.sp,
		)
		Text(
			text = value,
			modifier = Modifier.padding(top = 4.dp),
			color = when (tone) {
				Tone.INCOME -> colors.palm
				Tone.EXPENSE -> colors.coral
				Tone.NEUTRAL -> colors.ink
			},
			fontFamily = DisplayFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 18.sp,
		)
	}
}

@Composable
fun BudgetSummary(month: MonthKey, summary: MonthSummary, modifier: Modifier = Modifier) {
	val colors = islandColors
	val progress = if (summary.tasksTotal == 0) {
		0
	} else {
		(summary.tasksDone.toFloat() / summary.tasksTotal * 100).roundToInt()
	}
	val animatedProgress by animateFloatAsState(
		targetValue = progress / 100f,
		animationSpec = tween(durationMillis = 500),
		label = "progresso",
	)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.islandShell(colors)
			.padding(24.dp),
	) {
		IslandKicker(
			stringResource(R.string.budget_of, formatMonthLabel(month).lowercase(PT_BR)),
		)

		Text(
			text = formatBRL(summary.availableCents),
			modifier = Modifier.padding(top = 8.dp),
			color = if (summary.availableCents < 0) colors.coral else colors.ink,
			fontFamily = DisplayFamily,
			fontWeight = FontWeight.Bold,
			fontSize = 38.sp,
		)

		Text(
			text = stringResource(R.string.available_hint),
			modifier = Modifier.padding(top = 4.dp),
			color = colors.inkSoft,
			fontFamily = SansFamily,
			fontSize = 14.sp,
		)

		Column(modifier = Modifier.padding(top = 20.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Bottom,
			) {
				Text(
					text = stringResource(
						R.string.tasks_progress,
						summary.tasksDone,
						summary.tasksTotal,
					),
					color = colors.ink,
					fontFamily = SansFamily,
					fontWeight = FontWeight.SemiBold,
					fontSize = 14.sp,
				)
				Text(
					text = stringResource(R.string.percent, progress),
					color = colors.inkSoft,
					fontFamily = SansFamily,
					fontSize = 14.sp,
				)
			}

			Box(
				modifier = Modifier
					.padding(top = 8.dp)
					.fillMaxWidth()
					.height(8.dp)
					.clip(RoundedCornerShape(999.dp))
					.background(colors.chipLine),
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth(animatedProgress)
						.height(8.dp)
						.clip(RoundedCornerShape(999.dp))
						.background(Brush.horizontalGradient(listOf(colors.lagoon, colors.palm))),
				)
			}

			Row(modifier = Modifier.padding(top = 12.dp)) {
				Text(
					text = stringResource(R.string.projected_label),
					color = colors.inkSoft,
					fontFamily = SansFamily,
					fontSize = 14.sp,
				)
				Text(
					text = " " + formatBRL(summary.projectedCents),
					color = if (summary.projectedCents < 0) colors.coral else colors.ink,
					fontFamily = SansFamily,
					fontWeight = FontWeight.SemiBold,
					fontSize = 14.sp,
				)
			}
		}

		Column(
			modifier = Modifier.padding(top = 24.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Stat(
					label = stringResource(R.string.stat_received),
					value = formatBRL(summary.incomeDoneCents),
					tone = Tone.INCOME,
					modifier = Modifier.weight(1f),
				)
				Stat(
					label = stringResource(R.string.stat_to_receive),
					value = formatBRL(summary.incomePendingCents),
					tone = Tone.NEUTRAL,
					modifier = Modifier.weight(1f),
				)
			}
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Stat(
					label = stringResource(R.string.stat_paid),
					value = formatBRL(summary.expenseDoneCents),
					tone = Tone.EXPENSE,
					modifier = Modifier.weight(1f),
				)
				Stat(
					label = stringResource(R.string.stat_to_pay),
					value = formatBRL(summary.expensePendingCents),
					tone = Tone.NEUTRAL,
					modifier = Modifier.weight(1f),
				)
			}
		}
	}
}
