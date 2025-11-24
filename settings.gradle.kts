pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val agpVersion = "8.2.0"
        val kotlinVersion = "1.9.22"
        val googleServicesVersion = "4.4.2"
        val crashlyticsVersion = "3.0.2"
        val secretsVersion = "2.0.1"

        id("com.android.application") version agpVersion apply false
        id("com.android.library") version agpVersion apply false
        id("org.jetbrains.kotlin.android") version kotlinVersion apply false
        // Compose plugin not needed on AGP 8.2+ when using Compose BOM
        id("com.google.gms.google-services") version googleServicesVersion apply false
        id("com.google.firebase.crashlytics") version crashlyticsVersion apply false
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version secretsVersion apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "comkseblfaultapp"
include(":app")
