plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.compose)
    alias(builds.plugins.kotlinCompose)
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
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.koin.compose)

            implementation(compose.ui)

            implementation(projects.shared.foundation)
            implementation(projects.shared.resources)

            implementation(projects.logger)

            implementation(projects.libplayer.api)
        }
        androidMain.dependencies {

        }


        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }
    }
}

dependencies {

}


