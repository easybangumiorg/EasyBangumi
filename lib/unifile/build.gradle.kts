import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.kotlinxSerialization)
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
            api(libs.kotlinx.io)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            api(libs.okio)
            implementation(libs.kotlinx.serialization.json)

            implementation(projects.lib.utils)
            implementation(projects.logger)
            implementation(projects.lib.serialization)

            implementation(libs.koin.core)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.uni.file)
        }


        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}


dependencies {
}


