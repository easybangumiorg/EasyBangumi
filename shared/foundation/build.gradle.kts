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

    // 先预埋
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

    sourceSets {

        val commonMain by getting

        val jvmMain = create("jvmMain") {
            dependsOn(commonMain)
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
        }

        val androidMain by getting {
            dependsOn(jvmMain)
        }

        val iosMain = create("iosMain") {
            dependsOn(commonMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        commonMain.dependencies {
            implementation(compose.ui)

            implementation(libs.moko.resources.compose)
            implementation(libs.kotlin.reflect)

            implementation(compose.material3)

            implementation(libs.navigation.compose)

            implementation(libs.paging.multiplatform.common)
            implementation(libs.paging.multiplatform.compose)



            implementation(libs.coil.compose)
            implementation(libs.coil.ktor3)

            implementation(projects.shared.resources)
            implementation(projects.lib.logger)
            implementation(projects.lib.utils)
        }

        jvmMain.dependencies {
            implementation(libs.coil.svg)

        }

        iosMain.dependencies {

        }

        androidMain.dependencies {
            implementation(libs.moshi)
            implementation(libs.ktor.client.adroid)
            implementation(libs.coil)
            implementation(libs.coil.gif.android)
        }



        desktopMain.dependencies {
            implementation(libs.moshi)
            implementation(compose.desktop.currentOs)

            implementation(libs.ktor.client.java)

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.desktop.components.animatedImage)
            // implementation(compose.components.)
        }

    }
}

android {
    namespace = AppConfig.namespace + ".shared.foundation"
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






