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
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)

            implementation(projects.logger)

            implementation(projects.shared.data)

            implementation(projects.lib.utils)
            implementation(projects.lib.unifile)
            implementation(projects.lib.store)
        }


        jvmMain.dependencies {

        }

        androidMain.dependencies {

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