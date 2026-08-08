plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "app.gakseong"
    compileSdk = 35

    defaultConfig {
        // The Play and Firebase identity, deliberately not the same as `namespace` above.
        //
        // The Firebase project registered app.gakeseong, and an applicationId is permanent once published while
        // a namespace is only where the Kotlin lives. Changing this one line costs nothing; renaming every
        // package would have touched forty files to reach the same place.
        applicationId = "app.gakeseong"
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

    // The engine keeps its own assert-based check runnable by `kotlinc engine/*.kt`, so this source set covers
    // only the Android-side pure logic: the model, the rollover, and later the usage fold and the settle pipeline.
    sourceSets["test"].kotlin.srcDir("src/test/kotlin")

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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // §Stack: Firebase Anonymous Auth, Firestore, FCM. No server code.
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    // §Referral: only genuine app instances may write, and creates are rate-limited per inviter in rules.
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    // Play hands the referrer string over once, on first launch. A plain deep link cannot survive the Play
    // round-trip and Firebase Dynamic Links is shut down.
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    // §AI gate: the key lives in the Android keystore. Never logged, never in analytics.
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}
