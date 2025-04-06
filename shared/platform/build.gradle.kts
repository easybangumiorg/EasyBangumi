
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import plugin.easy_config.EasyConfigPlugin

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    id("EasyConfig")
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

        commonMain.dependencies {
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
        }

        val desktopMain by getting
        androidMain.dependencies {

        }

        desktopMain.dependencies {

        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".shared.platform"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}


easyBuildConfig {
    packageName.set(AppConfig.namespace)
    buildConfigFileName.set("EasyConfig")
    sourceDir.set(kotlin.sourceSets.findByName("commonMain")!!.kotlin)
    configProperties {
        "NAMESPACE" with AppConfig.namespace
        "VERSION_CODE" with AppConfig.versionCode
        "VERSION_NAME" with AppConfig.versionName
    }
}


dependencies {

}


