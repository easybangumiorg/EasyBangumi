plugins {
    alias(builds.plugins.kotlinJvm)
    id("EasyLibBuild")
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(projects.lib.unifile)
    implementation(projects.lib.utils)
    implementation(projects.lib.webviewJcef)
    implementation(projects.shared.sourceApi)

    implementation(projects.logger)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)
//    implementation(libs.jcef)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.log4j.core)
    testImplementation(libs.log4j.slf4j.impl)
}
