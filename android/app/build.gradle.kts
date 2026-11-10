plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }

android {
    namespace = "dev.neardrop.app"
    compileSdk = 36
    defaultConfig { applicationId = "dev.neardrop.app"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core")); implementation(project(":design-system")); implementation(project(":data"))
    implementation(project(":feature-home")); implementation(project(":feature-send")); implementation(project(":feature-receive")); implementation(project(":feature-devices")); implementation(project(":feature-settings"))
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
