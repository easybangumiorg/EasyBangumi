plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(builds.plugins.androidApplication) apply false
    alias(builds.plugins.androidLibrary) apply false

    alias(builds.plugins.kotlinCompose) apply false
    alias(builds.plugins.kotlinMultiplatform) apply false
    alias(builds.plugins.kotlinAndroid) apply false

    alias(builds.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.moko.resources.generator)
    }
}