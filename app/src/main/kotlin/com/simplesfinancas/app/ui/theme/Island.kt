package com.simplesfinancas.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Raio dos cartões: `rounded-3xl` do web. */
val IslandCorner: Dp = 24.dp

/**
 * `.island-shell`: vidro com borda fina, gradiente diagonal e sombra baixa e larga.
 */
fun Modifier.islandShell(
	colors: IslandColors,
	corner: Dp = IslandCorner,
): Modifier {
	val shape = RoundedCornerShape(corner)
	return this
		.shadow(
			elevation = 14.dp,
			shape = shape,
			ambientColor = Color(0xFF1E5A48).copy(alpha = 0.35f),
			spotColor = Color(0xFF173A40).copy(alpha = 0.35f),
		)
		.background(
			brush = Brush.linearGradient(
				colors = listOf(colors.surfaceStrong, colors.surface),
				start = Offset.Zero,
				end = Offset.Infinite,
			),
			shape = shape,
		)
		.border(1.dp, colors.line, shape)
}

/** `.chip`/`bg-[var(--chip-bg)]` com borda `--line`: pílulas, campos e botões secundários. */
fun Modifier.chipSurface(
	colors: IslandColors,
	corner: Dp = 12.dp,
	borderColor: Color = colors.line,
): Modifier {
	val shape = RoundedCornerShape(corner)
	return this
		.clip(shape)
		.background(colors.chipBg, shape)
		.border(1.dp, borderColor, shape)
}

/** `.page-wrap`: largura máxima de 1080px, centralizada. */
fun Modifier.pageWrap(): Modifier = this.fillMaxWidth().widthIn(max = 1080.dp)

private val RiseEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/** `.rise-in`: entra subindo 12px em 700ms. */
@Composable
fun Modifier.riseIn(delayMillis: Int = 0): Modifier {
	val progress = remember { Animatable(0f) }
	LaunchedEffect(Unit) {
		progress.animateTo(1f, tween(durationMillis = 700, delayMillis = delayMillis, easing = RiseEasing))
	}
	return this.graphicsLayer {
		alpha = progress.value
		translationY = (1f - progress.value) * 12.dp.toPx()
	}
}

/** `.island-kicker`: rótulo maiúsculo, espaçado e pequeno. */
@Composable
fun IslandKicker(text: String, modifier: Modifier = Modifier) {
	val colors = islandColors
	Text(
		text = text.uppercase(),
		modifier = modifier,
		color = colors.kicker,
		fontFamily = SansFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 11.sp,
		letterSpacing = 1.76.sp,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
	)
}

private fun DrawScope.glow(color: Color, cx: Float, cy: Float, rx: Float, ry: Float, stop: Float) {
	val center = Offset(size.width * cx, size.height * cy)
	withTransform({ scale(1f, ry / rx, pivot = center) }) {
		drawCircle(
			brush = Brush.radialGradient(
				colorStops = arrayOf(0f to color, stop to color.copy(alpha = 0f)),
				center = center,
				radius = rx,
			),
			radius = rx,
			center = center,
		)
	}
}

/**
 * O fundo do `body`: base em gradiente vertical mais os halos radiais da identidade.
 * (A grade de 28px do `body::after` ficou de fora — invisível em tela de celular.)
 */
@Composable
fun AppBackground(content: @Composable () -> Unit) {
	val colors = islandColors
	Box(Modifier.fillMaxSize().background(colors.bgBase)) {
		Canvas(Modifier.fillMaxSize()) {
			val topTint = lerp(colors.sand, Color.White, 0.32f)
			drawRect(
				Brush.verticalGradient(
					colorStops = arrayOf(0f to topTint, 0.44f to colors.foam, 1f to colors.bgBase),
				),
			)

			val unit = size.minDimension
			glow(colors.heroA, cx = -0.08f, cy = -0.10f, rx = unit * 1.2f, ry = unit * 0.68f, stop = 0.58f)
			glow(colors.heroB, cx = 1.12f, cy = -0.12f, rx = unit * 1.15f, ry = unit * 0.68f, stop = 0.62f)
			glow(colors.heroC, cx = 0.5f, cy = 1.15f, rx = unit * 0.8f, ry = unit * 0.42f, stop = 0.68f)
		}
		content()
	}
}
