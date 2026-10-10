package com.simplesfinancas.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.simplesfinancas.app.domain.auth.AccountSummary
import com.simplesfinancas.app.domain.buildMonthView
import com.simplesfinancas.app.lib.currentMonthKey
import com.simplesfinancas.app.lib.todayIso
import com.simplesfinancas.app.store.AuthStatus
import com.simplesfinancas.app.store.AuthStore
import com.simplesfinancas.app.store.FinanceStore
import com.simplesfinancas.app.ui.auth.AccountMenu
import com.simplesfinancas.app.ui.auth.LockScreen
import com.simplesfinancas.app.ui.theme.AppBackground
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.IslandKicker
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell
import com.simplesfinancas.app.ui.theme.pageWrap
import com.simplesfinancas.app.ui.theme.riseIn

/**
 * O porteiro: enquanto a sessão é restaurada mostra o esqueleto, sem sessão mostra a
 * trava, e com sessão mostra o painel do mês.
 */
@Composable
fun AppRoot(authStore: AuthStore, financeStore: FinanceStore) {
	val auth by authStore.state.collectAsStateWithLifecycle()

	AppBackground {
		when {
			auth.status == AuthStatus.LOADING -> LoadingShell()
			auth.user == null -> LockScreen(authStore)
			else -> Dashboard(
				user = requireNotNull(auth.user),
				store = financeStore,
				onSignOut = authStore::signOut,
			)
		}
	}
}

@Composable
private fun LoadingShell() {
	val colors = islandColors
	Box(
		modifier = Modifier.fillMaxSize().systemBarsPadding(),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = stringResource(R.string.loading),
			color = colors.inkSoft,
			fontFamily = SansFamily,
			fontSize = 14.sp,
		)
	}
}

@Composable
private fun EmptyState(onCreate: (TaskKind) -> Unit, onSeed: () -> Unit) {
	val colors = islandColors

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.islandShell(colors)
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box(
			modifier = Modifier
				.size(56.dp)
				.background(colors.palmSoft, RoundedCornerShape(16.dp)),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				Icons.Filled.AccountBalanceWallet,
				contentDescription = null,
				tint = colors.palm,
				modifier = Modifier.size(28.dp),
			)
		}

		Text(
			text = stringResource(R.string.empty_title),
			modifier = Modifier.padding(top = 20.dp),
			color = colors.ink,
			fontFamily = DisplayFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 24.sp,
			textAlign = TextAlign.Center,
		)

		Text(
			text = stringResource(R.string.empty_body),
			modifier = Modifier.padding(top = 8.dp),
			color = colors.inkSoft,
			fontFamily = SansFamily,
			fontSize = 15.sp,
			textAlign = TextAlign.Center,
		)

		Column(
			modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(12.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			PrimaryButton(
				text = stringResource(R.string.empty_first_expense),
				onClick = { onCreate(TaskKind.EXPENSE) },
				modifier = Modifier.fillMaxWidth(),
			)
			SecondaryButton(
				text = stringResource(R.string.empty_first_income),
				onClick = { onCreate(TaskKind.INCOME) },
				modifier = Modifier.fillMaxWidth(),
			)
			QuietButton(
				text = stringResource(R.string.empty_seed),
				onClick = onSeed,
				leading = {
					Icon(
						Icons.Filled.AutoAwesome,
						contentDescription = null,
						modifier = Modifier.size(16.dp),
					)
				},
			)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Dashboard(user: AccountSummary, store: FinanceStore, onSignOut: () -> Unit) {
	val colors = islandColors
	val state by store.state.collectAsStateWithLifecycle()

	var month by rememberSaveable { mutableStateOf(currentMonthKey()) }
	var editor by remember { mutableStateOf<TaskEditor?>(null) }
	var confirmingReset by rememberSaveable { mutableStateOf(false) }

	// Só depois da montagem — "hoje" não existe antes de a tela existir.
	var today by remember { mutableStateOf<String?>(null) }
	LaunchedEffect(Unit) { today = todayIso() }

	val view = remember(state, month) { buildMonthView(state, month) }
	val isEmpty = state.templates.isEmpty()

	Column(
		modifier = Modifier
			.fillMaxSize()
			.systemBarsPadding()
			.imePadding()
			.verticalScroll(rememberScrollState())
			.pageWrap()
			.padding(horizontal = 16.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.spacedBy(20.dp),
	) {
		Column(modifier = Modifier.riseIn()) {
			IslandKicker(stringResource(R.string.tagline))
			Text(
				text = stringResource(R.string.app_name),
				color = colors.ink,
				fontFamily = DisplayFamily,
				fontWeight = FontWeight.Bold,
				fontSize = 30.sp,
			)

			FlowRow(
				modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				AccountMenu(user = user, onSignOut = onSignOut)
				MonthNav(month = month, onChange = { month = it })
			}
		}

		if (isEmpty) {
			EmptyState(
				onCreate = { kind -> editor = TaskEditor.Create(kind) },
				onSeed = store::seedExamples,
			)
		} else {
			BudgetSummary(month = month, summary = view.summary)

			TaskList(
				kind = TaskKind.INCOME,
				title = stringResource(R.string.list_income),
				occurrences = view.income,
				totalCents = view.summary.incomeTotalCents,
				today = today,
				onToggle = { store.setOccurrenceDone(it.templateId, month, !it.done) },
				onEdit = { editor = openEditor(state.templates, it) ?: editor },
				onAdd = { editor = TaskEditor.Create(TaskKind.INCOME) },
			)

			TaskList(
				kind = TaskKind.EXPENSE,
				title = stringResource(R.string.list_expense),
				occurrences = view.expense,
				totalCents = view.summary.expenseTotalCents,
				today = today,
				onToggle = { store.setOccurrenceDone(it.templateId, month, !it.done) },
				onEdit = { editor = openEditor(state.templates, it) ?: editor },
				onAdd = { editor = TaskEditor.Create(TaskKind.EXPENSE) },
			)

			Row(
				modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (confirmingReset) {
					Text(
						text = stringResource(R.string.reset_ask),
						color = colors.inkSoft,
						fontFamily = SansFamily,
						fontSize = 12.sp,
					)
					QuietButton(
						text = stringResource(R.string.reset_yes),
						onClick = {
							store.resetAll()
							confirmingReset = false
						},
						color = colors.coral,
						fontSize = 12.sp,
					)
					QuietButton(
						text = stringResource(R.string.cancel),
						onClick = { confirmingReset = false },
						fontSize = 12.sp,
					)
				} else {
					QuietButton(
						text = stringResource(R.string.reset_all),
						onClick = { confirmingReset = true },
						fontSize = 12.sp,
					)
				}
			}
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
