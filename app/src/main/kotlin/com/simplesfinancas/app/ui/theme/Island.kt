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

/** Raio padrão de cartões e superfícies: limpo e refinado. */
val IslandCorner: Dp = 16.dp

/** Superfície minimalista: fundo limpo e borda sutil de 1dp. */
fun Modifier.islandShell(
	colors: IslandColors,
	corner: Dp = IslandCorner,
): Modifier {
	val shape = RoundedCornerShape(corner)
	return this
		.clip(shape)
		.background(colors.surface, shape)
		.border(1.dp, colors.line, shape)
}

/** Pílulas, campos e botões secundários limpos. */
fun Modifier.chipSurface(
	colors: IslandColors,
	corner: Dp = 10.dp,
	borderColor: Color = colors.line,
): Modifier {
	val shape = RoundedCornerShape(corner)
	return this
		.clip(shape)
		.background(colors.chipBg, shape)
		.border(1.dp, borderColor, shape)
}

/** Largura máxima centralizada. */
fun Modifier.pageWrap(): Modifier = this.fillMaxWidth().widthIn(max = 720.dp)

private val RiseEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/** Transição sutil de entrada. */
@Composable
fun Modifier.riseIn(delayMillis: Int = 0): Modifier {
	val progress = remember { Animatable(0f) }
	LaunchedEffect(Unit) {
		progress.animateTo(1f, tween(durationMillis = 350, delayMillis = delayMillis, easing = RiseEasing))
	}
	return this.graphicsLayer {
		alpha = progress.value
		translationY = (1f - progress.value) * 8.dp.toPx()
	}
}

/** Rótulo minimalista discreto. */
@Composable
fun IslandKicker(text: String, modifier: Modifier = Modifier) {
	val colors = islandColors
	Text(
		text = text.uppercase(),
		modifier = modifier,
		color = colors.inkSoft,
		fontFamily = SansFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 11.sp,
		letterSpacing = 1.sp,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
	)
}

/**
 * Fundo minimalista sólido e limpo.
 */
@Composable
fun AppBackground(content: @Composable () -> Unit) {
	val colors = islandColors
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(colors.bgBase),
	) {
		content()
	}
}
