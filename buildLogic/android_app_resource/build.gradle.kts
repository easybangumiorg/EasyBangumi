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
        create("AndroidAppResource") {
            id = "AndroidAppResource"
            implementationClass = "EasyAndroidAssetsPlugin"
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