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

//gradlePlugin {
//    plugins {
//        create("EasyConfig") {
//            id = "EasyConfig"
//            implementationClass = "plugin.easy_config.EasyConfigPlugin"
//            version = "1.0"
//        }
//    }
//}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(builds.kotlinPoet)
//    implementation(builds.androidGradlePlugin)

//    implementation(builds.androidGradlePlugin)
//    implementation(kotlin("script-runtime"))
//    implementation(libs.snakeyaml)

    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
//    implementation(builds.androidGradlePlugin)
//    api(builds.androidLibraryPlugin)
//    api(builds.androidApplicationPlugin)
//    api(builds.kotlinGradlePlugin)
//    api(builds.composePlugin)
//    api(builds.kotlinComposePlugin)
}


