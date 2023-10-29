plugins {
    id("java-library")
    alias(build.plugins.kotlin.jvm)
}
kotlin {
    jvmToolchain(17)

}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}