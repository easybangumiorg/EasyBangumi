
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

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(compose.desktop.currentOs)

    api(projects.libplayer.libplayerApi)
    api(projects.logger)

}
