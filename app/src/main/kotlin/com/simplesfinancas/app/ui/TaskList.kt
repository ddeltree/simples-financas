package com.simplesfinancas.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.Occurrence
import com.simplesfinancas.app.domain.TaskKind
import com.simplesfinancas.app.lib.formatBRL
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell

@Composable
private fun DoneToggle(
	occurrence: Occurrence,
	onToggle: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = islandColors
	val isIncome = occurrence.kind == TaskKind.INCOME
	val accent = if (isIncome) colors.palm else colors.coral
	val label = stringResource(R.string.cd_complete, occurrence.title)
	val checkAlpha by animateFloatAsState(
		targetValue = if (occurrence.done) 1f else 0f,
		label = "visto",
	)

	Box(
		modifier = modifier
			.size(22.dp)
			.clip(CircleShape)
			.background(if (occurrence.done) accent else colors.surface, CircleShape)
			.border(1.5.dp, if (occurrence.done) accent else colors.line, CircleShape)
			.semantics { contentDescription = label }
			.clickableRipple(onClick = onToggle),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			imageVector = Icons.Filled.Check,
			contentDescription = null,
			tint = colors.onAccent,
			modifier = Modifier.size(13.dp).alpha(checkAlpha),
		)
	}
}

@Composable
private fun TaskRow(
	occurrence: Occurrence,
	overdue: Boolean,
	onToggle: () -> Unit,
	onEdit: () -> Unit,
) {
	val colors = islandColors
	val isIncome = occurrence.kind == TaskKind.INCOME

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.clickableRipple(onClick = onEdit)
			.padding(vertical = 10.dp, horizontal = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		DoneToggle(occurrence = occurrence, onToggle = onToggle)

		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = occurrence.title,
				color = if (occurrence.done) colors.inkSoft.copy(alpha = 0.5f) else colors.ink,
				fontFamily = SansFamily,
				fontWeight = FontWeight.Medium,
				fontSize = 15.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				textDecoration = if (occurrence.done) TextDecoration.LineThrough else null,
			)

			Row(
				modifier = Modifier.padding(top = 2.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(6.dp),
			) {
				Text(
					text = "Dia ${occurrence.day}",
					color = if (overdue) colors.coral else colors.inkSoft,
					fontFamily = SansFamily,
					fontWeight = if (overdue) FontWeight.SemiBold else FontWeight.Normal,
					fontSize = 12.sp,
				)

				if (occurrence.recurring) {
					Icon(
						imageVector = Icons.Filled.Repeat,
						contentDescription = stringResource(R.string.badge_recurring),
						tint = colors.inkSoft,
						modifier = Modifier.size(12.dp),
					)
				}

				if (occurrence.adjusted) {
					Text(
						text = "• ajustado",
						color = colors.inkSoft,
						fontFamily = SansFamily,
						fontSize = 11.sp,
					)
				}
			}
		}

		Text(
			text = (if (isIncome) "+ " else "− ") + formatBRL(occurrence.amountCents),
			color = if (occurrence.done) {
				colors.inkSoft.copy(alpha = 0.5f)
			} else if (isIncome) {
				colors.palm
			} else {
				colors.coral
			},
			fontFamily = SansFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 14.sp,
		)
	}
}

@Composable
fun TaskList(
	kind: TaskKind,
	title: String,
	occurrences: List<Occurrence>,
	totalCents: Long,
	today: String?,
	onToggle: (Occurrence) -> Unit,
	onEdit: (Occurrence) -> Unit,
	onAdd: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = islandColors
	val isIncome = kind == TaskKind.INCOME

	Column(
		modifier = modifier
			.fillMaxWidth()
			.islandShell(colors)
			.padding(horizontal = 20.dp, vertical = 18.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = title,
				color = colors.ink,
				fontFamily = DisplayFamily,
				fontWeight = FontWeight.SemiBold,
				fontSize = 18.sp,
			)

			Text(
				text = formatBRL(totalCents),
				color = if (isIncome) colors.palm else colors.coral,
				fontFamily = SansFamily,
				fontWeight = FontWeight.SemiBold,
				fontSize = 15.sp,
			)
		}

		Box(
			modifier = Modifier
				.padding(top = 12.dp, bottom = 4.dp)
				.fillMaxWidth()
				.height(1.dp)
				.background(colors.line),
		)

		if (occurrences.isEmpty()) {
			Text(
				text = stringResource(R.string.list_empty),
				modifier = Modifier.padding(vertical = 16.dp),
				color = colors.inkSoft,
				fontFamily = SansFamily,
				fontSize = 14.sp,
			)
		} else {
			Column {
				occurrences.forEach { occurrence ->
					TaskRow(
						occurrence = occurrence,
						overdue = !occurrence.done && today != null && occurrence.date < today,
						onToggle = { onToggle(occurrence) },
						onEdit = { onEdit(occurrence) },
					)
				}
			}
		}

		Row(
			modifier = Modifier
				.padding(top = 8.dp)
				.fillMaxWidth()
				.clip(RoundedCornerShape(8.dp))
				.clickableRipple(onClick = onAdd)
				.padding(vertical = 10.dp, horizontal = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp),
		) {
			Icon(
				imageVector = Icons.Filled.Add,
				contentDescription = null,
				tint = colors.inkSoft,
				modifier = Modifier.size(16.dp),
			)
			Text(
				text = stringResource(
					if (isIncome) R.string.new_income else R.string.new_expense,
				),
				color = colors.inkSoft,
				fontFamily = SansFamily,
				fontWeight = FontWeight.Medium,
				fontSize = 13.sp,
			)
		}
	}
}
