package com.simplesfinancas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.simplesfinancas.app.ui.AppRoot
import com.simplesfinancas.app.ui.theme.SimplesFinancasTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		val container = (application as SimplesFinancasApp).container

		setContent {
			SimplesFinancasTheme {
				AppRoot(financeStore = container.financeStore)
			}
		}
	}
}
