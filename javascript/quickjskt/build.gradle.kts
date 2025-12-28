plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
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
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            api(libs.quickjs.kt)

            implementation(projects.logger)
        }

        jvmMain.dependencies {

        }

        androidMain.dependencies {

        }


        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

