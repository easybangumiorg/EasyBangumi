
plugins {
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.compose)
    alias(builds.plugins.kotlinAndroid)

    id("EasyLibBuild")
}
dependencies {
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(projects.libplayer.libplayerApi)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    implementation(compose.ui)

}