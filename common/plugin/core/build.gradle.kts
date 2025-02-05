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
    implementation(libs.kotlin.reflect)

    implementation(libs.zip4j)

    implementation(projects.model.cartoon)

    implementation(projects.common.plugin.api)
    implementation(projects.common.plugin.utils)

    implementation(projects.common.i18n)

    implementation(projects.lib.unifile)
    implementation(projects.lib.inject)

    implementation(projects.base.utils)
    implementation(projects.base.model)
}