package com.simplesfinancas.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.auth.CredentialsInput
import com.simplesfinancas.app.domain.auth.MIN_PASSWORD_LENGTH
import com.simplesfinancas.app.store.AuthStore
import com.simplesfinancas.app.ui.FieldLabel
import com.simplesfinancas.app.ui.IslandTextField
import com.simplesfinancas.app.ui.PrimaryButton
import com.simplesfinancas.app.ui.clickableRipple
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.IslandKicker
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.chipSurface
import com.simplesfinancas.app.ui.theme.islandColors
import com.simplesfinancas.app.ui.theme.islandShell
import com.simplesfinancas.app.ui.theme.pageWrap
import com.simplesfinancas.app.ui.theme.riseIn

private enum class Mode { SIGNIN, REGISTER }

@Composable
fun LockScreen(authStore: AuthStore) {
	val colors = islandColors
	val auth by authStore.state.collectAsStateWithLifecycle()

	var mode by remember {
		mutableStateOf(if (auth.accounts.isEmpty()) Mode.REGISTER else Mode.SIGNIN)
	}
	var name by remember { mutableStateOf(auth.accounts.firstOrNull()?.name ?: "") }
	var password by remember { mutableStateOf("") }
	var keepSignedIn by remember { mutableStateOf(false) }

	fun switchMode(next: Mode) {
		if (next == mode) return
		mode = next
		password = ""
		authStore.clearError()
	}

	fun submit() {
		if (auth.pending) return
		val input = CredentialsInput(name = name, password = password, remember = keepSignedIn)
		if (mode == Mode.SIGNIN) authStore.signIn(input) else authStore.register(input)
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.systemBarsPadding()
			.imePadding()
			.verticalScroll(rememberScrollState())
			.padding(16.dp),
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier
				.pageWrap()
				.widthIn(max = 384.dp)
				.riseIn()
				.islandShell(colors)
				.padding(28.dp),
		) {
			Box(
				modifier = Modifier.size(48.dp).background(colors.palmSoft, RoundedCornerShape(16.dp)),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					Icons.Filled.Lock,
					contentDescription = null,
					tint = colors.palm,
					modifier = Modifier.size(24.dp),
				)
			}

			IslandKicker(stringResource(R.string.tagline), Modifier.padding(top = 20.dp))
			Text(
				text = stringResource(R.string.app_name),
				color = colors.ink,
				fontFamily = DisplayFamily,
				fontWeight = FontWeight.Bold,
				fontSize = 24.sp,
			)
			Text(
				text = stringResource(
					if (mode == Mode.SIGNIN) {
						R.string.lock_hint_signin
					} else {
						R.string.lock_hint_register
					},
				),
				modifier = Modifier.padding(top = 8.dp),
				color = colors.inkSoft,
				fontFamily = SansFamily,
				fontSize = 14.sp,
			)

			Row(
				modifier = Modifier
					.padding(top = 24.dp)
					.fillMaxWidth()
					.chipSurface(colors)
					.padding(4.dp),
				horizontalArrangement = Arrangement.spacedBy(6.dp),
			) {
				ModeTab(
					text = stringResource(R.string.tab_signin),
					active = mode == Mode.SIGNIN,
					enabled = auth.accounts.isNotEmpty(),
					onClick = { switchMode(Mode.SIGNIN) },
					modifier = Modifier.weight(1f),
				)
				ModeTab(
					text = stringResource(R.string.tab_register),
					active = mode == Mode.REGISTER,
					onClick = { switchMode(Mode.REGISTER) },
					modifier = Modifier.weight(1f),
				)
			}

			Column(
				modifier = Modifier.padding(top = 20.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
			) {
				Column {
					FieldLabel(stringResource(R.string.field_name))
					IslandTextField(
						value = name,
						onValueChange = { name = it },
						modifier = Modifier.padding(top = 6.dp),
						keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
					)
				}

				Column {
					FieldLabel(stringResource(R.string.field_password))
					IslandTextField(
						value = password,
						onValueChange = { password = it },
						modifier = Modifier.padding(top = 6.dp),
						visualTransformation = PasswordVisualTransformation(),
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Password,
							imeAction = ImeAction.Done,
						),
						keyboardActions = KeyboardActions(onDone = { submit() }),
					)
					if (mode == Mode.REGISTER) {
						Text(
							text = stringResource(R.string.password_hint, MIN_PASSWORD_LENGTH),
							modifier = Modifier.padding(top = 6.dp),
							color = colors.inkSoft,
							fontFamily = SansFamily,
							fontSize = 12.sp,
						)
					}
				}

				Row(
					modifier = Modifier.clickableRipple { keepSignedIn = !keepSignedIn },
					verticalAlignment = Alignment.CenterVertically,
				) {
					Checkbox(
						checked = keepSignedIn,
						onCheckedChange = { keepSignedIn = it },
						colors = CheckboxDefaults.colors(
							checkedColor = colors.palm,
							uncheckedColor = colors.chipLine,
							checkmarkColor = colors.onAccent,
						),
					)
					Text(
						text = stringResource(R.string.remember_me),
						color = colors.inkSoft,
						fontFamily = SansFamily,
						fontSize = 14.sp,
					)
				}

				auth.error?.let {
					Text(
						text = it.message,
						color = colors.coral,
						fontFamily = SansFamily,
						fontWeight = FontWeight.Medium,
						fontSize = 14.sp,
					)
				}

				PrimaryButton(
					text = when {
						auth.pending -> stringResource(R.string.submitting)
						mode == Mode.SIGNIN -> stringResource(R.string.submit_signin)
						else -> stringResource(R.string.submit_register)
					},
					onClick = { submit() },
					enabled = !auth.pending,
					modifier = Modifier.fillMaxWidth(),
					leading = if (auth.pending) {
						{
							CircularProgressIndicator(
								modifier = Modifier.size(16.dp),
								color = colors.foam,
								strokeWidth = 2.dp,
							)
						}
					} else {
						null
					},
				)
			}

			Text(
				text = stringResource(R.string.lock_footer),
				modifier = Modifier.padding(top = 20.dp),
				color = colors.inkSoft,
				fontFamily = SansFamily,
				fontSize = 12.sp,
				lineHeight = 18.sp,
			)
		}
	}
}

@Composable
private fun ModeTab(
	text: String,
	active: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	val colors = islandColors
	Box(
		modifier = modifier
			.background(
				if (active) colors.ink else Color.Transparent,
				RoundedCornerShape(10.dp),
			)
			.clickableRipple(enabled = enabled, onClick = onClick)
			.padding(horizontal = 12.dp, vertical = 8.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			color = when {
				active -> colors.foam
				enabled -> colors.inkSoft
				else -> colors.inkSoft.copy(alpha = 0.4f)
			},
			fontFamily = SansFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 14.sp,
		)
	}
}
