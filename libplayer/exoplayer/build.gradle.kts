import org.jetbrains.compose.compose

plugins {
    alias(builds.plugins.androidLibrary)
//    alias(builds.plugins.compose)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)
    id("EasyLibBuild")
}

android {
    buildFeatures {
        compose = true
    }
}
dependencies {
    api(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    api(projects.libplayer.libplayerApi)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(projects.logger)

    implementation(compose("org.jetbrains.compose.ui:ui"))
//    implementation(compose.ui)

}