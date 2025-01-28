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

    implementation(projects.repository.cartoon)
    implementation(projects.common.plugin.api)

    implementation(projects.lib.unifile)
    implementation(projects.lib.inject)

    implementation(projects.base.utils)
    implementation(projects.base.model)
}