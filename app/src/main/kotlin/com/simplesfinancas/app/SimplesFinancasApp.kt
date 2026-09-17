package com.simplesfinancas.app

import android.app.Application
import com.simplesfinancas.app.auth.AppContainer

class SimplesFinancasApp : Application() {
	lateinit var container: AppContainer
		private set

	override fun onCreate() {
		super.onCreate()
		container = AppContainer(this)
	}
}
