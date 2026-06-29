plugins {
    alias(builds.plugins.kotlinJvm)
    id("EasyLibBuild")
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(projects.base)
    implementation(projects.logger)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(projects.webview.api)
    api(libs.jcef)
    api(libs.kcef)
}
