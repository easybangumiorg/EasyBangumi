plugins {
    alias(builds.plugins.kotlinJvm)
    id("EasyLibBuild")
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.slf4j.api)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)
    api(libs.kcef)
}
