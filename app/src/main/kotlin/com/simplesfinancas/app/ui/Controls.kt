package com.simplesfinancas.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.chipSurface
import com.simplesfinancas.app.ui.theme.islandColors

/** `bg-ink text-[var(--foam)]`: o botão sólido de ação principal. */
@Composable
fun PrimaryButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	leading: (@Composable () -> Unit)? = null,
) {
	val colors = islandColors
	Button(
		onClick = onClick,
		modifier = modifier,
		enabled = enabled,
		shape = RoundedCornerShape(12.dp),
		contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
		colors = ButtonDefaults.buttonColors(
			containerColor = colors.ink,
			contentColor = colors.foam,
			disabledContainerColor = colors.ink.copy(alpha = 0.6f),
			disabledContentColor = colors.foam.copy(alpha = 0.8f),
		),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			leading?.invoke()
			Text(text, fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
		}
	}
}

/** `border-line bg-[var(--chip-bg)]`: ação secundária. */
@Composable
fun SecondaryButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	contentColor: Color = islandColors.ink,
	borderColor: Color = islandColors.line,
) {
	val colors = islandColors
	Button(
		onClick = onClick,
		modifier = modifier,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, borderColor),
		contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
		colors = ButtonDefaults.buttonColors(
			containerColor = colors.chipBg,
			contentColor = contentColor,
		),
	) {
		Text(text, fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
	}
}

/** Botão de texto discreto (`text-ink-soft hover:text-ink`). */
@Composable
fun QuietButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	color: Color = islandColors.inkSoft,
	fontSize: TextUnit = 14.sp,
	leading: (@Composable () -> Unit)? = null,
) {
	TextButton(
		onClick = onClick,
		modifier = modifier,
		shape = RoundedCornerShape(12.dp),
		contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp),
		) {
			CompositionLocalProvider(LocalContentColor provides color) {
				leading?.invoke()
				Text(
					text,
					color = color,
					fontFamily = SansFamily,
					fontWeight = FontWeight.SemiBold,
					fontSize = fontSize,
				)
			}
		}
	}
}

/** Botão redondo só com ícone, no formato das pílulas de navegação. */
@Composable
fun CircleIconButton(
	icon: ImageVector,
	contentDescription: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	size: Dp = 36.dp,
	tint: Color = islandColors.inkSoft,
	bordered: Boolean = true,
) {
	val colors = islandColors
	val base = if (bordered) {
		Modifier
			.background(colors.chipBg, CircleShape)
			.border(1.dp, colors.line, CircleShape)
	} else {
		Modifier
	}

	Box(
		modifier = modifier
			.size(size)
			.then(base)
			.semantics { this.contentDescription = contentDescription }
			.clickableRipple(onClick = onClick),
		contentAlignment = Alignment.Center,
	) {
		Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.45f))
	}
}

/** Campo de texto no estilo da casa: cantos 12dp, borda `--line`, foco em lagoon. */
@Composable
fun IslandTextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	placeholder: String? = null,
	prefix: String? = null,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	singleLine: Boolean = true,
	textAlign: TextAlign? = null,
) {
	val colors = islandColors
	val selection = TextSelectionColors(
		handleColor = colors.lagoonDeep,
		backgroundColor = colors.lagoonDeep.copy(alpha = 0.3f),
	)

	CompositionLocalProvider(LocalTextSelectionColors provides selection) {
		OutlinedTextField(
			value = value,
			onValueChange = onValueChange,
			modifier = modifier.fillMaxWidth(),
			textStyle = TextStyle(
				fontFamily = SansFamily,
				fontSize = 16.sp,
				color = colors.ink,
				textAlign = textAlign ?: TextAlign.Start,
			),
			placeholder = placeholder?.let {
				{ Text(it, color = colors.inkSoft.copy(alpha = 0.6f), fontFamily = SansFamily) }
			},
			prefix = prefix?.let {
				{ Text(it, color = colors.inkSoft, fontFamily = SansFamily, fontSize = 14.sp) }
			},
			singleLine = singleLine,
			shape = RoundedCornerShape(12.dp),
			keyboardOptions = keyboardOptions,
			keyboardActions = keyboardActions,
			visualTransformation = visualTransformation,
			colors = OutlinedTextFieldDefaults.colors(
				focusedContainerColor = colors.surfaceStrong,
				unfocusedContainerColor = colors.surfaceStrong,
				focusedBorderColor = colors.lagoonDeep,
				unfocusedBorderColor = colors.line,
				cursorColor = colors.lagoonDeep,
				focusedTextColor = colors.ink,
				unfocusedTextColor = colors.ink,
			),
		)
	}
}

/** Rótulo de campo: `text-[0.68rem] uppercase tracking-[0.12em] text-ink-soft`. */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
	Text(
		text = text.uppercase(),
		modifier = modifier,
		color = islandColors.inkSoft,
		fontFamily = SansFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 11.sp,
		letterSpacing = 1.32.sp,
	)
}

/** Controle segmentado do diálogo — uma fileira de opções mutuamente exclusivas. */
@Composable
fun <T> SegmentedControl(
	value: T,
	options: List<Pair<T, String>>,
	onChange: (T) -> Unit,
	label: String,
	modifier: Modifier = Modifier,
) {
	val colors = islandColors
	Row(
		modifier = modifier
			.fillMaxWidth()
			.semantics { contentDescription = label },
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		options.forEach { (optionValue, optionLabel) ->
			val active = optionValue == value
			Box(
				modifier = Modifier
					.weight(1f)
					.chipSurface(
						colors = colors,
						borderColor = if (active) colors.lagoonDeep else colors.line,
					)
					.background(
						if (active) colors.palmSoft else Color.Transparent,
						RoundedCornerShape(12.dp),
					)
					.clickableRipple { onChange(optionValue) }
					.padding(horizontal = 12.dp, vertical = 10.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = optionLabel,
					color = if (active) colors.ink else colors.inkSoft,
					fontFamily = SansFamily,
					fontWeight = FontWeight.SemiBold,
					fontSize = 14.sp,
					maxLines = 1,
				)
			}
		}
	}
}
