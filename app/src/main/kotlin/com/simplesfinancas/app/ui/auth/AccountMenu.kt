package com.simplesfinancas.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import com.simplesfinancas.app.ui.CircleIconButton
import com.simplesfinancas.app.ui.theme.SansFamily
import com.simplesfinancas.app.ui.theme.islandColors

@Composable
fun AccountMenu(user: AccountSummary, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
	val colors = islandColors

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		Icon(
			imageVector = Icons.Filled.Person,
			contentDescription = null,
			tint = colors.inkSoft,
			modifier = Modifier.size(16.dp),
		)

		Text(
			text = user.name,
			modifier = Modifier.widthIn(max = 120.dp),
			color = colors.ink,
			fontFamily = SansFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 13.sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)

		CircleIconButton(
			icon = Icons.AutoMirrored.Filled.Logout,
			contentDescription = stringResource(R.string.sign_out),
			onClick = onSignOut,
			size = 28.dp,
			bordered = false,
		)
	}
}
