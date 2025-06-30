
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
            implementation(projects.logger)
            implementation(projects.shared.platform)
            implementation(libs.koin.core)
            implementation(libs.koin.test)
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.logback.android)
        }

        jvmMain.dependencies {

        }

        desktopMain.dependencies {
            implementation(libs.log4j.core)
            implementation(libs.log4j.slf4j.impl)
        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}


