
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
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.serialization.kotlinx.xml)
            implementation(libs.ktor.client.content.negotiation)


            implementation(libs.koin.core)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(projects.lib.utils)
            implementation(projects.lib.store)
            implementation(projects.lib.unifile)

            implementation(projects.logger)
            implementation(libs.ktor.client.logging)
        }

        jvmMain.dependencies {
//            implementation(libs.ktor.client.plugins.cookies)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(projects.lib.webviewWebkit)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.java)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(projects.lib.webviewJcef)
        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}


