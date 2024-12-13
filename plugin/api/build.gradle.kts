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
    implementation(projects.inject)
    implementation(projects.repository)
    implementation(libs.kotlinx.coroutines.core)
}