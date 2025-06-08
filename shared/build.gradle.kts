@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
}
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
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

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended) // 此依赖需要在生产环境中进行剪枝，非常巨大
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.moko.resources.compose)
            implementation(libs.coil.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.paging.multiplatform.common)
            implementation(libs.paging.multiplatform.compose)
            implementation(libs.md3.window.size)

            api(projects.lib)

            api(projects.shared.data)
            api(projects.shared.plugin)
            api(projects.shared.theme)
            api(projects.shared.foundation)
            api(projects.shared.resources)
            api(projects.shared.platform)
            api(projects.shared.preference)
            api(projects.shared.scheme)
            api(projects.shared.debug)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(compose.uiTest)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.ui)
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}




dependencies {

}


