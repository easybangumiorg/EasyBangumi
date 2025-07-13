import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
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
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.koin.core)
            api(libs.kache)
            api(libs.kache.file)

            implementation(projects.logger)
            implementation(projects.lib.serialization)
            implementation(projects.lib.unifile)
            implementation(projects.lib.utils)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.preference.ktx)
        }
        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        iosMain.dependencies {

        }
    }
}



dependencies {

}


