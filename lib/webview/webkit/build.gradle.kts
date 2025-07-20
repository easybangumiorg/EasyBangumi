
plugins {
    alias(builds.plugins.androidLibrary)
//    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)

    id("EasyLibBuild")
}
dependencies {

    implementation(projects.lib.unifile)
    implementation(projects.lib.utils)
    implementation(projects.logger)
    implementation(projects.lib.webviewApi)


    implementation(libs.commons.text)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.webkit)

}