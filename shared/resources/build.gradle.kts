import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.mokoResources)
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

        androidMain.dependencies {
        }
        commonMain.dependencies {
            api(libs.moko.resources)
        }
        desktopMain.dependencies {

        }

    }
}
val namespace = extra.get("easy.build.showNamespace").toString()

multiplatformResources {
    resourcesPackage.set("$namespace.shared.resources") // required
    resourcesClassName.set("Res") // optional, default MR
}
