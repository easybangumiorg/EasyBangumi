
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinxAtomicfu)
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
            implementation(libs.coil.ktor3)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kache.file)

            implementation(projects.shared.platform)
            implementation(projects.shared.ktor)
            implementation(projects.shared.data)
            implementation(projects.shared.resources)
            implementation(projects.logger)

            implementation(projects.lib.store)
            implementation(projects.lib.utils)
            implementation(projects.lib.unifile)

            implementation(libs.ksoup)

            implementation(projects.shared.sourceApi)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
        }
        iosMain.dependencies {

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.logging)
            implementation(projects.test)
            implementation(libs.koin.test)
        }
    }
}

dependencies {

}


