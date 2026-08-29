@file:Suppress("UnstableApiUsage")
import com.heyanle.buildsrc.Android

plugins {
    // com.android.test ships on the classpath via the baselineprofile plugin's
    // transitive AGP dependency, so it must be applied without a version here.
    id("com.android.test")
    alias(build.plugins.kotlin.android)
    alias(build.plugins.baselineprofile)
}

android {
    namespace = "com.heyanle.easybangumi4.baselineprofile"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = 26
        targetSdk = Android.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        // The app module ships debug/release/performance. Mirror `performance` here so
        // generatePerformanceBaselineProfile can drive the locally-installable
        // R8-optimized variant (release APK is unsigned and cannot be installed).
        create("performance") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
}

dependencies {
    implementation(libs.macrobenchmark)
    implementation(libs.uiautomator)
    implementation(libs.junit)
}
