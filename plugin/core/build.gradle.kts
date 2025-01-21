plugins {
    alias(builds.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(17)

}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.io)

    implementation(libs.zip4j)


    implementation(projects.lib.inject)
    implementation(projects.repository.cartoon)
    implementation(projects.plugin.api)
    implementation(projects.lib.unifile)

    implementation(projects.base.utils)
}