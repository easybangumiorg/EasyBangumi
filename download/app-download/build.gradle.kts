
import com.heyanle.buildsrc.Android

plugins {
    alias(build.plugins.android.application)
    alias(build.plugins.kotlin.android)
    alias(build.plugins.ksp)
}

android {
    namespace = "com.heyanle.app_download"
    compileSdk = Android.compileSdk


    defaultConfig {
        applicationId = "com.heyanle.app_download"
        minSdk = 24
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }


    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = build.versions.compose.compiler.get()
    }
}

dependencies {

    implementation(androidx.medie)

    implementation(androidx.google.material)

    implementation(androidx.webkit)

    implementation(androidx.window)

    implementation(androidx.bundles.core)
    androidTestImplementation (androidx.bundles.test.core)
    implementation(androidx.google.material)
    implementation(compose.bundles.ui)
    implementation(compose.bundles.runtime)
    implementation(compose.bundles.animation)
    implementation(compose.bundles.foundation)
    implementation(compose.bundles.material)
    implementation(compose.bundles.material3)

    implementation(libs.ffmpeg.kit)

    ksp(libs.aria.compiler)
    annotationProcessor(libs.aria.compiler)
    implementation(libs.aria)
    implementation(libs.aria.m3u8)

    testImplementation(libs.junit)

    implementation(androidx.bundles.room.impl)
    implementation(androidx.room.paging)
    annotationProcessor(androidx.room.compiler)
    ksp(androidx.room.compiler)
    testImplementation(androidx.room.testing)
    androidTestImplementation(androidx.room.testing)

    implementation(libs.jeff.m3u8)

}