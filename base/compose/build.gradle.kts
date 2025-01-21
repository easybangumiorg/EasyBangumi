import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.moshi)
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.ui)

            implementation(libs.moshi)
            implementation(libs.navigation.compose)

            implementation(projects.lib.inject)
            implementation(projects.lib.unifile)

        }
        desktopMain.dependencies {
            implementation(libs.moshi)
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".compose_base"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}




dependencies {

}






