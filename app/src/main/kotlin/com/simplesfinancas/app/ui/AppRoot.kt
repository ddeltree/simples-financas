package com.simplesfinancas.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.Occurrence
import com.simplesfinancas.app.domain.TaskKind
import com.simplesfinancas.app.domain.TaskTemplate
import com.simplesfinancas.app.domain.buildMonthView
import com.simplesfinancas.app.lib.addMonths
import com.simplesfinancas.app.lib.currentMonthKey
import com.simplesfinancas.app.lib.formatMonthLabel
import com.simplesfinancas.app.lib.todayIso
import com.simplesfinancas.app.store.AuthStore
import com.simplesfinancas.app.store.FinanceStore
import com.simplesfinancas.app.ui.theme.AppBackground
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell
import com.simplesfinancas.app.ui.theme.pageWrap

@Composable
fun AppRoot(
	authStore: AuthStore? = null,
	financeStore: FinanceStore,
) {
	AppBackground {
		Dashboard(store = financeStore)
	}
}

@Composable
private fun EmptyState(onCreate: (TaskKind) -> Unit, onSeed: () -> Unit) {
	val colors = islandColors

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.islandShell(colors)
			.padding(horizontal = 24.dp, vertical = 28.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(R.string.empty_title),
			color = colors.ink,
			fontFamily = DisplayFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 18.sp,
			textAlign = TextAlign.Center,
		)

		Text(
			text = stringResource(R.string.empty_body),
			modifier = Modifier.padding(top = 8.dp),
			color = colors.inkSoft,
			fontFamily = SansFamily,
			fontSize = 14.sp,
			textAlign = TextAlign.Center,
		)

		Column(
			modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(10.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			PrimaryButton(
				text = stringResource(R.string.empty_first_income),
				onClick = { onCreate(TaskKind.INCOME) },
				modifier = Modifier.fillMaxWidth(),
			)
			SecondaryButton(
				text = stringResource(R.string.empty_first_expense),
				onClick = { onCreate(TaskKind.EXPENSE) },
				modifier = Modifier.fillMaxWidth(),
			)
			QuietButton(
				text = stringResource(R.string.empty_seed),
				onClick = onSeed,
			)
		}
	}
}

@Composable
private fun Dashboard(store: FinanceStore) {
	val colors = islandColors
	val state by store.state.collectAsStateWithLifecycle()

	var month by rememberSaveable { mutableStateOf(currentMonthKey()) }
	var editor by remember { mutableStateOf<TaskEditor?>(null) }

	var today by remember { mutableStateOf<String?>(null) }
	LaunchedEffect(Unit) { today = todayIso() }

	val isEmpty = state.templates.isEmpty()
	var totalDragX by remember { mutableFloatStateOf(0f) }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.systemBarsPadding()
			.imePadding(),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.pointerInput(month) {
					detectHorizontalDragGestures(
						onDragStart = { totalDragX = 0f },
						onHorizontalDrag = { _, dragAmount ->
							totalDragX += dragAmount
						},
						onDragEnd = {
							if (totalDragX < -50f) {
								month = addMonths(month, 1)
							} else if (totalDragX > 50f) {
								month = addMonths(month, -1)
							}
						},
					)
				}
				.verticalScroll(rememberScrollState())
				.pageWrap()
				.padding(horizontal = 16.dp, vertical = 16.dp),
		) {
			AnimatedContent(
				targetState = month,
				transitionSpec = {
					val forward = targetState > initialState
					val slideIn = slideInHorizontally(
						animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
					) { width -> if (forward) width else -width } + fadeIn(
						animationSpec = tween(durationMillis = 200),
					)
					val slideOut = slideOutHorizontally(
						animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
					) { width -> if (forward) -width else width } + fadeOut(
						animationSpec = tween(durationMillis = 200),
					)
					slideIn togetherWith slideOut
				},
				label = "monthTransition",
			) { currentMonth ->
				val currentView = remember(state, currentMonth) { buildMonthView(state, currentMonth) }

				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(16.dp),
				) {
					// Mês sutil no topo
					Text(
						text = formatMonthLabel(currentMonth),
						modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
						color = colors.inkSoft,
						fontFamily = SansFamily,
						fontWeight = FontWeight.Medium,
						fontSize = 14.sp,
						textAlign = TextAlign.Center,
					)

					// Montante simples e centralizado, com fonte grande e sinal (+ / -)
					BudgetSummary(month = currentMonth, summary = currentView.summary)

					if (isEmpty) {
						EmptyState(
							onCreate = { kind -> editor = TaskEditor.Create(kind) },
							onSeed = store::seedExamples,
						)
					} else {
						TaskList(
							kind = TaskKind.INCOME,
							title = stringResource(R.string.list_income),
							occurrences = currentView.income,
							totalCents = currentView.summary.incomeTotalCents,
							today = today,
							onToggle = { store.setOccurrenceDone(it.templateId, currentMonth, !it.done) },
							onEdit = { editor = openEditor(state.templates, it) ?: editor },
						)

						TaskList(
							kind = TaskKind.EXPENSE,
							title = stringResource(R.string.list_expense),
							occurrences = currentView.expense,
							totalCents = currentView.summary.expenseTotalCents,
							today = today,
							onToggle = { store.setOccurrenceDone(it.templateId, currentMonth, !it.done) },
							onEdit = { editor = openEditor(state.templates, it) ?: editor },
						)

						// Espaço final para não sobrepor o botão flutuante
						Spacer(modifier = Modifier.padding(bottom = 72.dp))
					}
				}
			}
		}

		// Botão flutuante único no canto inferior direito
		FloatingActionButton(
			onClick = { editor = TaskEditor.Create(TaskKind.EXPENSE) },
			containerColor = colors.ink,
			contentColor = colors.foam,
			shape = CircleShape,
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(24.dp),
		) {
			Icon(
				imageVector = Icons.Filled.Add,
				contentDescription = stringResource(R.string.new_expense),
				modifier = Modifier.size(24.dp),
			)
		}
	}

	editor?.let { current ->
		TaskDialog(
			editor = current,
			month = month,
			store = store,
			onClose = { editor = null },
		)
	}
}

/** Abrir o editor exige o modelo por trás da ocorrência; sem ele não há o que editar. */
private fun openEditor(
	templates: List<TaskTemplate>,
	occurrence: Occurrence,
): TaskEditor? =
	templates.find { it.id == occurrence.templateId }
		?.let { TaskEditor.Edit(occurrence, it) }
