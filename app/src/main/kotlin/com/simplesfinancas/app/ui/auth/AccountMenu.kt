package com.simplesfinancas.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplesfinancas.app.R
import com.simplesfinancas.app.domain.auth.AccountSummary
import com.simplesfinancas.app.ui.QuietButton
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.chipSurface
import com.simplesfinancas.app.ui.theme.islandColors

@Composable
fun AccountMenu(user: AccountSummary, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
	val colors = islandColors

	Row(
		modifier = modifier
			.chipSurface(colors)
			.padding(start = 10.dp, end = 2.dp, top = 4.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Box(
			modifier = Modifier.size(24.dp).background(colors.palmSoft, CircleShape),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				Icons.Filled.Person,
				contentDescription = null,
				tint = colors.palm,
				modifier = Modifier.size(14.dp),
			)
		}

		Text(
			text = user.name,
			modifier = Modifier.widthIn(max = 128.dp),
			color = colors.ink,
			fontFamily = SansFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 14.sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)

		QuietButton(
			text = stringResource(R.string.sign_out),
			onClick = onSignOut,
			fontSize = 12.sp,
			leading = {
				Icon(
					Icons.AutoMirrored.Filled.Logout,
					contentDescription = null,
					modifier = Modifier.size(14.dp),
				)
			},
		)
	}
}
