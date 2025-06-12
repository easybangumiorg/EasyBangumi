
plugins {
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)

    id("EasyLibBuild")
}
dependencies {
    implementation(libs.media3.exoplayer)
    implementation(projects.libplayer.api)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)

}