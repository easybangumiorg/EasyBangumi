plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(builds.plugins.kotlinJvm)
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("EasyLibBuild") {
            id = "EasyLibBuild"
            implementationClass = "EasyLibBuildPlugin"
            version = "1.0"
        }
    }
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(builds.kotlinPoet)
    implementation(builds.androidGradlePlugin)
    implementation(builds.kotlinGradlePlugin)
    implementation(builds.androidLibraryPlugin)
    implementation(builds.kotlinComposePlugin)
    implementation(builds.androidApplicationPlugin)
    implementation(builds.composePlugin)
}