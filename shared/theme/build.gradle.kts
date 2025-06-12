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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.navigation.compose)

            implementation(projects.shared.resources)

            implementation(projects.lib.store)
            implementation(projects.lib.utils)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}


