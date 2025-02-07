import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.mokoResources)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop"){
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
        }
        commonMain.dependencies {
            api(libs.moko.resources)
        }
        desktopMain.dependencies {

        }

    }
}

multiplatformResources {
    resourcesPackage.set(AppConfig.namespace + ".common.resources") // required
    resourcesClassName.set("Res") // optional, default MR
}

android {
    namespace = AppConfig.namespace + ".common.resources"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

//compose.resources {
//    publicResClass = true
//    packageOfResClass = AppConfig.namespace + ".common.resources"
//    generateResClass = always
//}