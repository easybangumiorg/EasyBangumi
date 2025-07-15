plugins {
    alias(builds.plugins.kotlinJvm)
    id("EasyLibBuild")
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(projects.lib.unifile)
    implementation(projects.lib.utils)
    implementation(projects.logger)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)
//    implementation(libs.jcef)
}
