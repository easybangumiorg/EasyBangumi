plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
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
            api(projects.logger)
            api(libs.kotlinx.io)
            api(libs.okio)
            api(libs.kotlinx.datetime)
            api(libs.kache)
            api(libs.kache.file)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.paging.multiplatform.common)


        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.preference.ktx)
            implementation(libs.uni.file)
        }
        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        jvmMain.dependencies {
            implementation(libs.moshi)
        }
        iosMain.dependencies {

        }
    }
}



dependencies {

}


