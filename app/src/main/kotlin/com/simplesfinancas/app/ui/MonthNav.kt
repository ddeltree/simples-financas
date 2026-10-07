package com.simplesfinancas.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.R
import com.simplesfinancas.app.lib.MonthKey
import com.simplesfinancas.app.lib.addMonths
import com.simplesfinancas.app.lib.currentMonthKey
import com.simplesfinancas.app.lib.formatMonthLabel
import com.simplesfinancas.app.ui.theme.DisplayFamily
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.chipSurface
import com.simplesfinancas.app.ui.theme.islandColors

@Composable
fun MonthNav(month: MonthKey, onChange: (MonthKey) -> Unit, modifier: Modifier = Modifier) {
	val colors = islandColors
	val isCurrent = month == currentMonthKey()

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
	) {
		CircleIconButton(
			icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
			contentDescription = stringResource(R.string.cd_previous_month),
			onClick = { onChange(addMonths(month, -1)) },
			size = 32.dp,
		)

		Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
			Text(
				text = formatMonthLabel(month),
				color = colors.ink,
				fontFamily = DisplayFamily,
				fontWeight = FontWeight.SemiBold,
				fontSize = 16.sp,
				textAlign = TextAlign.Center,
				maxLines = 1,
			)
		}

		CircleIconButton(
			icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
			contentDescription = stringResource(R.string.cd_next_month),
			onClick = { onChange(addMonths(month, 1)) },
			size = 32.dp,
		)

		if (!isCurrent) {
			Box(
				modifier = Modifier
					.padding(start = 8.dp)
					.chipSurface(colors, corner = 6.dp)
					.clickableRipple { onChange(currentMonthKey()) }
					.padding(horizontal = 8.dp, vertical = 4.dp),
			) {
				Text(
					text = stringResource(R.string.today),
					color = colors.inkSoft,
					fontFamily = SansFamily,
					fontWeight = FontWeight.Medium,
					fontSize = 11.sp,
				)
			}
		}
	}
}
