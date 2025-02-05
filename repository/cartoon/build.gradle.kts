import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
        }
        commonMain.dependencies {

            implementation(projects.model.cartoon)

            implementation(projects.base.model)

            implementation(projects.lib.inject)

            implementation(libs.sqlite.bundled)
            implementation(libs.androidx.room.runtime)

        }
        desktopMain.dependencies {
        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".shared"
    compileSdk = 35
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





