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

            api(projects.logger)
            api(projects.lib.serialization)
            api(projects.lib.unifile)
            api(projects.lib.utils)
            api(projects.lib.store)


        }

        commonTest.dependencies {
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


