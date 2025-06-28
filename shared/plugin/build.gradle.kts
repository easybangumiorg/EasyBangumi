
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
            implementation(libs.kotlin.reflect)
            implementation(libs.koin.core)
            implementation(libs.paging.multiplatform.common)

            implementation(projects.shared.data)
            implementation(projects.shared.resources)

            api(projects.shared.plugin.pluginApi)
            api(projects.shared.plugin.pluginBangumi)

            implementation(projects.logger)
            implementation(projects.lib.unifile)
            implementation(projects.lib.store)
            implementation(projects.lib.utils)
        }


        jvmMain.dependencies {
            implementation(projects.javascript.rhino)
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


