import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    // alias(libs.plugins.sqldeight)
}

group = "com.heyanle.easy_bangumi_cm.sqldelight"
version = "1.0.0"

kotlin {
    androidTarget() {
        compilerOptions {
            this@compilerOptions.jvmTarget.set(
                JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")

    sourceSets {


        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)

        }

        nativeMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }


        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        desktopMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }



    }
}


android {
    namespace = "com.heyanle.easy_bangumi_cm.sqldelight"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
