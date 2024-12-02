import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.ksp)
}

group = "com.heyanle.easy_bangumi_cm.room"
version = "1.0.0"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")

    sourceSets {


        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.datetime)
        }

        iosMain.dependencies {

        }


        androidMain.dependencies {

        }
        desktopMain.dependencies {

        }



    }
}


android {
    namespace = "com.heyanle.easy_bangumi_cm.room"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

