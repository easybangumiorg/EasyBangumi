@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    alias(builds.plugins.ksp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinxAtomicfu)
    alias(libs.plugins.mokoResources)
    id("EasyLibBuild")
}

kotlin {
    sourceSets {

        val commonMain by getting
        val commonTest by getting
        val jvmMain by getting
        val desktopMain by getting
        val desktopTest by getting
        val androidMain by getting
        val iosMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.material)
            implementation(libs.compose.material3)
            implementation(libs.compose.animation)
            implementation(libs.compose.animation.graphics)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            api(libs.moko.resources)
            implementation(libs.moko.resources.compose)
            implementation(libs.coil.compose)
            api(libs.coil.ktor3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlin.reflect)
            implementation(libs.paging.multiplatform.common)
            implementation(libs.paging.multiplatform.compose)
            implementation(libs.md3.window.size)
            implementation(libs.ksoup)
            implementation(libs.kache.file)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.serialization.kotlinx.xml)

            api(libs.androidx.lifecycle.runtime.compose)
            api(libs.androidx.lifecycle.viewmodel.compose)

            api(projects.base)
            api(projects.webview.api)
            api(projects.webview)
            api(projects.libplayer.libplayerApi)
            implementation(projects.logger)
            implementation(projects.javascript.quickjsKt)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.logging)
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
        }

        jvmMain.dependencies {
            implementation(libs.coil.svg)
            implementation(projects.javascript.rhino)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.ui)
            implementation(libs.moshi)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.compose.desktop.components.animated.image)
            implementation(projects.webview.jcef)
            api(projects.libplayer.libplayerVlcj)
        }

        desktopTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.logging)
            implementation(libs.log4j.core)
            implementation(libs.log4j.slf4j.impl)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.moshi)
            implementation(libs.ktor.client.android)
            implementation(libs.coil)
            implementation(libs.coil.gif.android)
            implementation(libs.preference.ktx)
            implementation(projects.webview.webkit)
            implementation(projects.libplayer.libplayerExoplayer)
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

val namespace = extra.get("easy.build.namespace").toString()

multiplatformResources {
    resourcesPackage.set("$namespace.shared.resources")
    resourcesClassName.set("Res")
}

tasks.register<Test>("jvmTest") {
    useJUnitPlatform()
}

tasks.register<Test>("androidUnitTest") {
}
