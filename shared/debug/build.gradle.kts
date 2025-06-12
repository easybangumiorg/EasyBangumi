
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended) // 此依赖需要在生产环境中进行剪枝，非常巨大
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(projects.libplayer.api)
            implementation(projects.lib)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.ui)
            implementation(projects.libplayer.vlcj)
        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}


