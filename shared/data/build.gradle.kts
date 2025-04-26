
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import plugin.easy_config.EasyConfigPlugin

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.ksp)
}
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

    }

    // 提前预埋保证 commonMain 是纯 kotlin 环境
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        val commonMain by getting

        val jvmMain = create("jvmMain") {
            dependsOn(commonMain)
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
        }

        val androidMain by getting {
            dependsOn(jvmMain)
        }

        val iosMain = create("iosMain") {
            dependsOn(commonMain)
        }


        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        commonMain.dependencies {

            implementation(libs.kotlinx.datetime)

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

android {
    namespace = AppConfig.namespace + ".shared.data"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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



