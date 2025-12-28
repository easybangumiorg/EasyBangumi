import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyLibBuild")
}

kotlin {
    sourceSets {

        val commonMain by getting
        val jvmMain by getting
        val desktopMain by getting
        val androidMain by getting
        val iosMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.materialIconsExtended) // 此依赖需要在生产环境中进行剪枝，非常巨大

            implementation(libs.moko.resources.compose)
            implementation(libs.kotlin.reflect)

            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.animationGraphics)

            api(libs.androidx.lifecycle.runtime.compose)
            api(libs.androidx.lifecycle.viewmodel.compose)

            implementation(libs.navigation.compose)

            implementation(libs.paging.multiplatform.common)
            implementation(libs.paging.multiplatform.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.coil.compose)
            api(libs.coil.ktor3)

            implementation(projects.shared.data)
//            implementation(projects.shared.plugin)
            implementation(projects.shared.resources)
            implementation(projects.shared.scheme)
            implementation(projects.shared.source)

            implementation(projects.logger)
            implementation(projects.lib.utils)
        }

        jvmMain.dependencies {
            implementation(libs.coil.svg)

        }

        iosMain.dependencies {

        }

        androidMain.dependencies {
            implementation(libs.moshi)
            implementation(libs.ktor.client.android)
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


dependencies {

}






