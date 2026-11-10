plugins { id("com.android.library"); id("org.jetbrains.kotlin.plugin.compose") }
android { namespace = "dev.neardrop.design"; compileSdk = 36; defaultConfig { minSdk = 26 }; buildFeatures { compose = true } }
dependencies { implementation(platform("androidx.compose:compose-bom:2024.12.01")); implementation("androidx.compose.material3:material3"); implementation("androidx.compose.ui:ui") }
