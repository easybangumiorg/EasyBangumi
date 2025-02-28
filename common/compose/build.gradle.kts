import org.jetbrains.compose.ExperimentalComposeLibrary
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

    jvm("desktop"){
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.moshi)
            implementation(libs.ktor.client.adroid)
            implementation(libs.coil)
            implementation(libs.coil.gif.android)
        }
        commonMain.dependencies {
            implementation(compose.ui)

            implementation(compose.material3)

            implementation(projects.base.utils)
            implementation(projects.base.service)

            implementation(projects.common.resources)
            implementation(projects.lib.inject)
            implementation(projects.lib.unifile)

            implementation(libs.moshi)
            implementation(libs.navigation.compose)

            implementation(libs.paging.multiplatform.common)
            implementation(libs.paging.multiplatform.compose)
            implementation(libs.moko.resources.compose)


            implementation(libs.coil.compose)
            implementation(libs.coil.ktor3)
            implementation(libs.coil.svg)

        }
        desktopMain.dependencies {
            implementation(libs.moshi)
            implementation(compose.desktop.currentOs)

            implementation(libs.ktor.client.java)

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.desktop.components.animatedImage)
            // implementation(compose.components.)
        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".common.compose"
    compileSdk = 35
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






