plugins {
    alias(builds.plugins.kotlinJvm)
}

group = AppConfig.namespace
version = AppConfig.versionName

kotlin {
    jvmToolchain(21)

}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(projects.lib.logger)
    implementation(projects.lib.utils)




}
