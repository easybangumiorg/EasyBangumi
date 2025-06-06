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
        create("EasyConfig") {
            id = "EasyConfig"
            implementationClass = "EasyConfigPlugin"
            version = "1.0"
        }
    }
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(builds.kotlinPoet)
    implementation(builds.androidGradlePlugin)
}