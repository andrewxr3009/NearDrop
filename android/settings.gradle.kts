pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "NearDrop"
include(":app", ":core", ":design-system", ":feature-home", ":feature-send", ":feature-receive", ":feature-devices", ":feature-settings", ":data")
