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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
			.size(24.dp)
			.clip(CircleShape)
			.background(if (occurrence.done) accent else colors.surfaceStrong, CircleShape)
			.border(1.dp, if (occurrence.done) accent else colors.chipLine, CircleShape)
			.semantics { contentDescription = label }
			.clickableRipple(onClick = onToggle),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			imageVector = Icons.Filled.Check,
			contentDescription = null,
			tint = colors.onAccent,
			modifier = Modifier.size(14.dp).alpha(checkAlpha),
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
			.clip(RoundedCornerShape(16.dp))
			.clickableRipple(onClick = onEdit)
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		DoneToggle(occurrence, onToggle)

		// O dia vira pílula coral quando venceu e continua pendente. No web o motivo vinha
		// num `title`; aqui vai na semântica, que é onde o leitor de tela o encontra.
		val overdueHint = stringResource(R.string.overdue_hint)
		Box(
			modifier = Modifier
				.width(36.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(if (overdue) colors.coralSoft else colors.chipBg)
				.then(
					if (overdue) {
						Modifier.semantics { contentDescription = overdueHint }
					} else {
						Modifier
					},
				)
				.padding(vertical = 4.dp),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = occurrence.day.toString().padStart(2, '0'),
				color = if (overdue) colors.coral else colors.inkSoft,
				fontFamily = SansFamily,
				fontWeight = FontWeight.Bold,
				fontSize = 12.sp,
				textAlign = TextAlign.Center,
			)
		}

		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = occurrence.title,
				color = colors.ink.copy(alpha = if (occurrence.done) 0.55f else 1f),
				fontFamily = SansFamily,
				fontWeight = FontWeight.Medium,
				fontSize = 15.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				textDecoration = if (occurrence.done) TextDecoration.LineThrough else null,
			)

			if (occurrence.recurring || occurrence.adjusted) {
				Row(
					modifier = Modifier.padding(top = 2.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					if (occurrence.recurring) {
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(4.dp),
						) {
							Icon(
								Icons.Filled.Repeat,
								contentDescription = null,
								tint = colors.inkSoft,
								modifier = Modifier.size(12.dp),
							)
							Text(
								text = stringResource(R.string.badge_recurring),
								color = colors.inkSoft,
								fontFamily = SansFamily,
								fontSize = 11.sp,
							)
						}
					}
					if (occurrence.adjusted) {
						Text(
							text = stringResource(R.string.badge_adjusted),
							color = colors.lagoonDeep,
							fontFamily = SansFamily,
							fontSize = 11.sp,
						)
					}
				}
			}
		}

		Text(
			text = (if (isIncome) "+ " else "− ") + formatBRL(occurrence.amountCents),
			color = if (occurrence.done) {
				if (isIncome) colors.palm else colors.coral
			} else {
				colors.inkSoft
			},
			fontFamily = SansFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 14.sp,
		)

		// Sem hover no celular: o lápis fica sempre visível.
		CircleIconButton(
			icon = Icons.Filled.Edit,
			contentDescription = stringResource(R.string.cd_edit, occurrence.title),
			onClick = onEdit,
			size = 28.dp,
			bordered = false,
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
	val pending = occurrences.count { !it.done }

	Column(
		modifier = modifier
			.fillMaxWidth()
			.islandShell(colors)
			.padding(20.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.Bottom,
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = title,
					color = colors.ink,
					fontFamily = DisplayFamily,
					fontWeight = FontWeight.SemiBold,
					fontSize = 20.sp,
				)
				Text(
					text = if (occurrences.isEmpty()) {
						stringResource(R.string.list_empty)
					} else {
						pluralStringResource(R.plurals.pending_of, pending, pending, occurrences.size)
					},
					modifier = Modifier.padding(top = 2.dp),
					color = colors.inkSoft,
					fontFamily = SansFamily,
					fontSize = 12.sp,
				)
			}

			Text(
				text = formatBRL(totalCents),
				color = if (isIncome) colors.palm else colors.coral,
				fontFamily = DisplayFamily,
				fontWeight = FontWeight.SemiBold,
				fontSize = 18.sp,
			)
		}

		Box(
			modifier = Modifier
				.padding(top = 16.dp)
				.fillMaxWidth()
				.height(1.dp)
				.background(colors.line),
		)

		Column(modifier = Modifier.padding(top = 8.dp)) {
			occurrences.forEach { occurrence ->
				TaskRow(
					occurrence = occurrence,
					overdue = !occurrence.done && today != null && occurrence.date < today,
					onToggle = { onToggle(occurrence) },
					onEdit = { onEdit(occurrence) },
				)
			}
		}

		Row(
			modifier = Modifier
				.padding(top = 12.dp)
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.dashedBorder(colors.chipLine)
				.clickableRipple(onClick = onAdd)
				.padding(horizontal = 12.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Icon(
				Icons.Filled.Add,
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
				fontWeight = FontWeight.SemiBold,
				fontSize = 14.sp,
			)
		}
	}
}
