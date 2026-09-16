package com.simplesfinancas.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * O web tinha `hover:`; aqui o feedback é o ripple do toque. Existe como nome próprio
 * para que a intenção ("isso responde ao toque") fique legível nos componentes.
 */
fun Modifier.clickableRipple(enabled: Boolean = true, onClick: () -> Unit): Modifier =
	this.clickable(enabled = enabled, onClick = onClick)

/** `border-dashed` do web — o Compose não tem borda tracejada pronta. */
fun Modifier.dashedBorder(
	color: Color,
	corner: Dp = 16.dp,
	width: Dp = 1.dp,
): Modifier = this.drawBehind {
	drawRoundRect(
		color = color,
		cornerRadius = CornerRadius(corner.toPx()),
		style = Stroke(
			width = width.toPx(),
			pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
		),
	)
}
