plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(builds.plugins.kotlinJvm)
}


repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("EasyConfig") {
            id = "EasyConfig"
            implementationClass = "plugin.easy_config.EasyConfigPlugin"
            version = "1.0"
        }
    }
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(builds.kotlinPoet)
}

