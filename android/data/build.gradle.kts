plugins { id("com.android.library") }
android { namespace = "dev.neardrop.data"; compileSdk = 36; defaultConfig { minSdk = 26 } }
dependencies { implementation(project(":core")); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0"); implementation("com.google.android.gms:play-services-nearby:19.3.0"); testImplementation("junit:junit:4.13.2"); testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0") }
