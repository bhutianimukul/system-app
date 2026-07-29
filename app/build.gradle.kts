plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "app.gakseong"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.gakseong"
        // 26 is the floor: Health Connect needs it, and UsageStatsManager is well behaved from here up.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }

    // ponytail: the phase-01 engine stays in its own directory rather than moving under app/. It has no Android
    // dependencies and `kotlinc engine/*.kt` must keep working, which is the whole point of it being pure.
    sourceSets["main"].kotlin.srcDir("../engine")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    // Glance for the home-screen widgets. Not Compose: a widget renders through RemoteViews, so it gets its own
    // composables and cannot share the ui/Kit.kt components.
    implementation("androidx.glance:glance-appwidget:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
