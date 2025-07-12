
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
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)

            implementation(projects.lib)
            implementation(projects.shared.data)


        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {

        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}


