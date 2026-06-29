
plugins {
    alias(builds.plugins.androidLibrary)
//    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)

    id("EasyLibBuild")
}
dependencies {

    implementation(projects.base)
    implementation(projects.logger)
    implementation(projects.webview.api)


    implementation(libs.commons.text)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.webkit)

}
