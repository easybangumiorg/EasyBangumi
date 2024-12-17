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

    implementation(projects.inject)
    implementation(libs.zip4j)
    implementation(projects.repository)
    implementation(projects.plugin.api)
    implementation(projects.unifile)
    implementation(projects.base)
    //api(projects.plugin.)
}