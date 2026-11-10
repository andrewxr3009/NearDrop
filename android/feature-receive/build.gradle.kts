plugins { id("com.android.library"); id("org.jetbrains.kotlin.plugin.compose") }
android { namespace = "dev.neardrop.feature.receive"; compileSdk = 36; defaultConfig { minSdk = 26 }; buildFeatures { compose = true } }
dependencies { implementation(project(":core")); implementation(project(":data")); implementation(project(":design-system")); implementation(platform("androidx.compose:compose-bom:2024.12.01")); implementation("androidx.compose.material3:material3"); implementation("androidx.compose.ui:ui") }
