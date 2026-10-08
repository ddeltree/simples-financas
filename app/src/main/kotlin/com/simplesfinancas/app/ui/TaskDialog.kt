package com.simplesfinancas.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.Occurrence
import com.simplesfinancas.app.domain.Recurrence
import com.simplesfinancas.app.domain.TaskKind
import com.simplesfinancas.app.domain.TaskTemplate
import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.PT_BR
import com.simplesfinancas.app.lib.centsToInputValue
import com.simplesfinancas.app.lib.clampDay
import com.simplesfinancas.app.lib.formatMonthLabel
import com.simplesfinancas.app.lib.parseAmountToCents
import com.simplesfinancas.app.store.FinanceStore
import com.simplesfinancas.app.store.TemplateInput
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell
import java.time.LocalDate

/** Alvo do diálogo: criar uma tarefa nova ou editar a ocorrência aberta. */
sealed interface TaskEditor {
	data class Create(val kind: TaskKind) : TaskEditor

	data class Edit(val occurrence: Occurrence, val template: TaskTemplate) : TaskEditor
}

private enum class Scope { ALL, MONTH }

@Composable
fun TaskDialog(
	editor: TaskEditor,
	month: MonthKey,
	store: FinanceStore,
	onClose: () -> Unit,
) {
	val colors = islandColors
	val template = (editor as? TaskEditor.Edit)?.template
	val occurrence = (editor as? TaskEditor.Edit)?.occurrence
	val isEdit = template != null

	var kind by remember { mutableStateOf(occurrence?.kind ?: (editor as TaskEditor.Create).kind) }
	var title by remember { mutableStateOf(occurrence?.title ?: "") }
	var amount by remember {
		mutableStateOf(occurrence?.let { centsToInputValue(it.amountCents) } ?: "")
	}
	var day by remember { mutableStateOf(occurrence?.day?.toString() ?: "") }
	var recurring by remember { mutableStateOf(occurrence?.recurring ?: true) }
	var scope by remember { mutableStateOf(Scope.ALL) }
	var showOptions by remember { mutableStateOf(false) }
	var error by remember { mutableStateOf<String?>(null) }
	var confirmingDelete by remember { mutableStateOf(false) }

	val monthLabel = formatMonthLabel(month)
	val monthName = monthLabel.substringBefore(" ")

	val errorTitle = stringResource(R.string.error_title)
	val errorAmount = stringResource(R.string.error_amount)
	val errorDay = stringResource(R.string.error_day)

	fun submit() {
		val trimmed = title.trim()
		if (trimmed.isEmpty()) {
			error = errorTitle
			return
		}

		val amountCents = parseAmountToCents(amount)
		if (amountCents == null || amountCents <= 0) {
			error = errorAmount
			return
		}

		val dayOfMonth = if (day.trim().isEmpty()) {
			null
		} else {
			val parsed = day.trim().toIntOrNull()
			if (parsed == null || parsed !in 1..31) {
				error = errorDay
				return
			}
			clampDay(month, parsed)
		}

		val recurrence = if (recurring) {
			Recurrence.Monthly(dayOfMonth)
		} else {
			Recurrence.Once(dayOfMonth)
		}

		if (template == null) {
			store.addTemplate(
				TemplateInput(
					kind = kind,
					title = trimmed,
					amountCents = amountCents,
					recurrence = recurrence,
					startMonth = month,
				),
			)
			onClose()
			return
		}

		val changedRecurrenceType = recurring != (template.recurrence is Recurrence.Monthly)

		store.updateTemplate(
			id = template.id,
			kind = kind,
			title = trimmed,
			recurrence = recurrence,
			startMonth = if (changedRecurrenceType) month else null,
			clearEndMonth = changedRecurrenceType,
			amountCents = if (scope == Scope.ALL) amountCents else null,
		)

		store.setOccurrenceAmount(
			template.id,
			month,
			if (scope == Scope.MONTH) amountCents else null,
		)

		onClose()
	}

	Dialog(
		onDismissRequest = onClose,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		BackHandler(enabled = confirmingDelete) { confirmingDelete = false }

		Column(
			modifier = Modifier
				.padding(16.dp)
				.widthIn(max = 440.dp)
				.fillMaxWidth()
				.islandShell(colors)
				.verticalScroll(rememberScrollState())
				.padding(20.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(
						if (isEdit) R.string.dialog_edit_kicker else R.string.dialog_new_kicker,
					),
					color = colors.ink,
					fontFamily = DisplayFamily,
					fontWeight = FontWeight.SemiBold,
					fontSize = 17.sp,
					modifier = Modifier.weight(1f),
				)

				CircleIconButton(
					icon = Icons.Filled.Close,
					contentDescription = stringResource(R.string.cd_close),
					onClick = onClose,
					size = 28.dp,
					bordered = false,
				)
			}

			Column(
				modifier = Modifier.padding(top = 16.dp),
				verticalArrangement = Arrangement.spacedBy(14.dp),
			) {
				SegmentedControl(
					value = kind,
					options = listOf(
						TaskKind.INCOME to stringResource(R.string.kind_income),
						TaskKind.EXPENSE to stringResource(R.string.kind_expense),
					),
					onChange = { kind = it },
					label = stringResource(R.string.field_kind_a11y),
				)

				Column {
					FieldLabel(stringResource(R.string.field_description))
					IslandTextField(
						value = title,
						onValueChange = { title = it },
						modifier = Modifier.padding(top = 4.dp),
						placeholder = stringResource(
							if (kind == TaskKind.INCOME) {
								R.string.placeholder_income
							} else {
								R.string.placeholder_expense
							},
						),
						keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
					)
				}

				Column {
					FieldLabel(stringResource(R.string.field_amount))
					IslandTextField(
						value = amount,
						onValueChange = { amount = it },
						modifier = Modifier.padding(top = 4.dp),
						placeholder = stringResource(R.string.amount_placeholder),
						prefix = stringResource(R.string.currency_prefix),
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Decimal,
							imeAction = if (showOptions) ImeAction.Next else ImeAction.Done,
						),
					)
				}

				// Opções colapsáveis (Data, Repetição, Escopo)
				QuietButton(
					text = stringResource(if (showOptions) R.string.less_options else R.string.more_options),
					onClick = { showOptions = !showOptions },
					fontSize = 13.sp,
					leading = {
						Icon(
							imageVector = if (showOptions) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
							contentDescription = null,
							modifier = Modifier.size(16.dp),
						)
					},
				)

				AnimatedVisibility(visible = showOptions) {
					Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
						Column {
							FieldLabel(stringResource(R.string.field_day))
							IslandTextField(
								value = day,
								onValueChange = { day = it.filter(Char::isDigit).take(2) },
								modifier = Modifier.padding(top = 4.dp),
								placeholder = stringResource(R.string.placeholder_day),
								keyboardOptions = KeyboardOptions(
									keyboardType = KeyboardType.Number,
									imeAction = ImeAction.Done,
								),
							)
						}

						Column {
							FieldLabel(stringResource(R.string.field_recurrence))
							SegmentedControl(
								value = recurring,
								options = listOf(
									true to stringResource(R.string.recurrence_monthly),
									false to stringResource(R.string.recurrence_once, monthName),
								),
								onChange = { recurring = it },
								label = stringResource(R.string.field_recurrence),
								modifier = Modifier.padding(top = 4.dp),
							)
						}

						if (isEdit && recurring) {
							Column {
								FieldLabel(stringResource(R.string.field_scope))
								SegmentedControl(
									value = scope,
									options = listOf(
										Scope.ALL to stringResource(R.string.scope_all),
										Scope.MONTH to stringResource(
											R.string.scope_month,
											monthName.lowercase(PT_BR),
										),
									),
									onChange = { scope = it },
									label = stringResource(R.string.field_scope_a11y),
									modifier = Modifier.padding(top = 4.dp),
								)
							}
						}
					}
				}
			}

			error?.let {
				Text(
					text = it,
					modifier = Modifier.padding(top = 12.dp),
					color = colors.coral,
					fontFamily = SansFamily,
					fontWeight = FontWeight.Medium,
					fontSize = 13.sp,
				)
			}

			if (confirmingDelete && template != null) {
				Column(
					modifier = Modifier
						.padding(top = 16.dp)
						.fillMaxWidth()
						.background(colors.coralSoft, RoundedCornerShape(12.dp))
						.border(1.dp, colors.coral.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
						.padding(14.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					Text(
						text = stringResource(R.string.delete_ask),
						color = colors.ink,
						fontFamily = SansFamily,
						fontWeight = FontWeight.SemiBold,
						fontSize = 13.sp,
					)

					if (template.recurrence is Recurrence.Monthly) {
						SecondaryButton(
							text = stringResource(
								R.string.delete_end_after,
								monthLabel.lowercase(PT_BR),
							),
							onClick = {
								store.endTemplateAfter(template.id, month)
								onClose()
							},
							modifier = Modifier.fillMaxWidth(),
						)
					}

					PrimaryButton(
						text = stringResource(R.string.delete_all_months),
						onClick = {
							store.deleteTemplate(template.id)
							onClose()
						},
						modifier = Modifier.fillMaxWidth(),
					)

					QuietButton(
						text = stringResource(R.string.cancel),
						onClick = { confirmingDelete = false },
					)
				}
			} else {
				Row(
					modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					if (isEdit) {
						QuietButton(
							text = stringResource(R.string.remove),
							onClick = { confirmingDelete = true },
							color = colors.coral,
							leading = {
								Icon(
									Icons.Filled.Delete,
									contentDescription = null,
									modifier = Modifier.size(16.dp),
								)
							},
						)
					} else {
						Spacer(Modifier.size(1.dp))
					}

					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp),
					) {
						QuietButton(stringResource(R.string.cancel), onClose)
						PrimaryButton(stringResource(R.string.save), onClick = { submit() })
					}
				}
			}
		}
	}
}
