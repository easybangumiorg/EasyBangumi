import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

group = "com.heyanle.easy_bangumi_cm.component.room"
version = "1.0.0"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    jvm()
    jvm("desktop")

    sourceSets {

        val commonMain by getting
        val jvmMain by getting {
            dependsOn(commonMain)
        }
        val androidMain by getting {
            dependsOn(jvmMain)
        }
        val desktopMain by getting {
            dependsOn(jvmMain)
        }

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
        }

        iosMain.dependencies {

        }

        jvmMain.dependencies {

        }

        androidMain.dependencies {

        }
        desktopMain.dependencies {

        }



    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.heyanle.easy_bangumi_cm.component.room"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
