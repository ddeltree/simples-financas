plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.simplesfinancas.app"
	compileSdk = 36

	defaultConfig {
		applicationId = "com.simplesfinancas.app"
		// 26 dá java.time e PBKDF2WithHmacSHA256 sem desugaring.
		minSdk = 26
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	buildFeatures {
		compose = true
	}

	sourceSets {
		getByName("main").kotlin.srcDir("src/main/kotlin")
		getByName("test").kotlin.srcDir("src/test/kotlin")
	}
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.runtime.compose)
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.kotlinx.serialization.json)

	implementation(platform(libs.compose.bom))
	implementation(libs.compose.ui)
	implementation(libs.compose.ui.graphics)
	implementation(libs.compose.ui.tooling.preview)
	implementation(libs.compose.material3)
	implementation(libs.compose.material.icons.extended)
	debugImplementation(libs.compose.ui.tooling)

	testImplementation(libs.junit)
	testImplementation(libs.kotlinx.coroutines.test)
}
