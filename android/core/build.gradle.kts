plugins { id("com.android.library"); id("org.jetbrains.kotlin.plugin.compose") }
android { namespace = "dev.neardrop.core"; compileSdk = 36; defaultConfig { minSdk = 26 }; buildFeatures { compose = true } }
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.12.01")); implementation("androidx.compose.ui:ui")
    testImplementation("junit:junit:4.13.2")
}
