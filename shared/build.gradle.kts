@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinxAtomicfu)
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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons.extended) // 此依赖需要在生产环境中进行剪枝，非常巨大
            implementation(libs.compose.ui)
            implementation(libs.compose.material)
            implementation(libs.compose.material3)
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
            implementation(libs.kotlinx.serialization.json)
//            implementation(libs.kotlinx.atomicfu.gradle)

            api(projects.lib)
//
//            api(projects.shared.plugin)
//            api(projects.shared.plugin.pluginBangumi)

            api(projects.shared.source)


            api(projects.shared.data)
            api(projects.shared.theme)
            api(projects.shared.foundation)
            api(projects.shared.resources)
            api(projects.shared.platform)
            api(projects.shared.preference)
            api(projects.shared.scheme)
//            api(projects.shared.debug)
            api(projects.shared.playcon)
            api(projects.shared.ktor)
//            api(projects.shared.ui)

            api(projects.libplayer.libplayerApi)


        }

//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//            implementation(compose.uiTest)
//        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.ui)
            api(projects.libplayer.libplayerVlcj)

        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(projects.libplayer.libplayerExoplayer)
        }
        
        iosMain.dependencies {

        }
    }
}


dependencies {
}


