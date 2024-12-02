import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)

    alias(builds.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
//            implementation(compose.preview)
//            implementation(libs.androidx.activity.compose)
//            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(projects.base)
//            implementation(compose.runtime)
//            implementation(compose.foundation)
//            implementation(compose.material)
//            implementation(compose.ui)
//            implementation(compose.components.resources)
//            implementation(compose.components.uiToolingPreview)
//            implementation(libs.androidx.lifecycle.viewmodel)
//            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.koin.core)
//            implementation(libs.kotlinx.io)
//            implementation(libs.kotlinx.datetime)
//
//            implementation(libs.androidx.room.runtime)
//            implementation(libs.sqlite.bundled)
        }
        desktopMain.dependencies {
//            implementation(compose.desktop.currentOs)
//            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = AppConfig.namespace + ".shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}




dependencies {
    // debugImplementation(compose.uiTooling)
}




