@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyLibBuild")
}

dependencies {
    api(libs.vlcj)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.desktop.currentOs)

    implementation(projects.libplayer.api)
    implementation(projects.logger)

}
