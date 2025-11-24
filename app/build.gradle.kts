// Load secrets from secrets.properties file
import java.util.Properties
import java.io.FileInputStream

val secretsProperties = Properties().apply {
    load(FileInputStream(rootProject.file("secrets.properties")))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.ksebl.comkseblfaultapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ksebl.comkseblfaultapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        // Enable vector drawable support
        vectorDrawables.useSupportLibrary = true
        
        // Enable multidex
        multiDexEnabled = true
        
        // Test runner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // BuildConfig fields
        buildConfigField("String", "API_BASE_URL", "\"${secretsProperties["API_BASE_URL"] ?: ""}\"")
        buildConfigField("String", "API_KEY", "\"${secretsProperties["API_KEY"] ?: ""}\"")
        
        // ResValue for string usage
        resValue("string", "google_maps_key", "${secretsProperties["MAPS_API_KEY"] ?: ""}")

        // Manifest placeholders
        manifestPlaceholders += mapOf(
            "MAPS_API_KEY" to (secretsProperties["MAPS_API_KEY"] ?: "")
        )
        
        // Enable Java 8 features
        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose-utils:4.3.3")

    // Desugar JDK
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // DI & Logging
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}