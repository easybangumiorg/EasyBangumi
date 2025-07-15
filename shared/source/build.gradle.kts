
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.kotlinxAtomicfu)
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
            implementation(libs.coil.ktor3)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ksoup)

            implementation(projects.shared.platform)
            implementation(projects.shared.ktor)
            implementation(projects.shared.data)
            implementation(projects.shared.resources)
            implementation(projects.logger)
            api(projects.shared.sourceApi)
            api(projects.shared.sourceBangumi)
            implementation(projects.lib)

        }

        jvmMain.dependencies {
            implementation(projects.javascript.rhino)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
//            implementation(libs.jcef)
//
//            implementation("me.friwi:jcef-natives-linux-amd64:jcef-ca49ada+cef-135.0.20+ge7de5c3+chromium-135.0.7049.85")
//            implementation("me.friwi:jcef-natives-macosx-amd64:jcef-ca49ada+cef-135.0.20+ge7de5c3+chromium-135.0.7049.85")
//            implementation("me.friwi:jcef-natives-macosx-arm64:jcef-ca49ada+cef-135.0.20+ge7de5c3+chromium-135.0.7049.85")
//            implementation("me.friwi:jcef-natives-windows-amd64:jcef-ca49ada+cef-135.0.20+ge7de5c3+chromium-135.0.7049.85")
        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}


