import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    alias(libs.plugins.kotlinxSerialization)
    alias(builds.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.moshi)
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.ui)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.moshi)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(projects.inject)
            implementation(projects.unifile)

        }
        desktopMain.dependencies {
            implementation(libs.moshi)
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}




dependencies {

}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

val kspMetaDataList = listOf(
    "kspCommonMainMetadata",
    "kspAndroid",
    "kspDesktop",
)

dependencies {
    kspMetaDataList.forEach {
        add(it, libs.androidx.room.compiler)
    }
}





