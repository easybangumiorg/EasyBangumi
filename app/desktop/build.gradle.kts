import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.daemon.common.FileSystem

plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyConfig")
}

group = AppConfig.namespace
version = AppConfig.versionName


dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview funx tionality


    implementation(compose.desktop.currentOs)

    implementation(projects.shared)
    implementation(projects.lib.logger)

    implementation(libs.koin.core)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j.impl)

    implementation(libs.vlcj)
    implementation(projects.player.vlcj)

}

kotlin {
    jvmToolchain(21)

    sourceSets {

    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}



compose.desktop {

    application {

        mainClass = "org.easybangumi.next.MainKt"

        buildTypes.release.proguard {
            optimize.set(false)
            obfuscate.set(false)
            isEnabled.set(false)
        }

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("../assets"))

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = AppConfig.namespace
            packageVersion = AppConfig.versionName

        }
    }
}

easyBuildConfig {
    packageName.set(AppConfig.namespace)
    buildConfigFileName.set("EasyConfig")
    sourceDir.set(kotlin.sourceSets.findByName("main")!!.kotlin)


    configProperties {
        "NAMESPACE" with AppConfig.namespace
        "VERSION_CODE" with AppConfig.versionCode
        "VERSION_NAME" with AppConfig.versionName
    }
}
