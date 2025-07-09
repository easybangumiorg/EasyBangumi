
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.ksp)
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

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        commonMain.dependencies {

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(libs.koin.core)

            implementation(projects.lib.store)
            implementation(projects.lib.unifile)
            implementation(projects.lib.utils)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {

        }
        iosMain.dependencies {

        }
    }
}


ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

val kspMetaDataList = listOf(
    "kspCommonMainMetadata",
    "kspAndroid",
    "kspDesktop",
    "kspIosSimulatorArm64",
    "kspIosX64",
    "kspIosArm64",
)

dependencies {
    kspMetaDataList.forEach {
        add(it, libs.androidx.room.compiler)
    }
}



