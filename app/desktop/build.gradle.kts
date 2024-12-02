import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
}

group = AppConfig.namespace
version = AppConfig.versionName

val projectVersion by extra("")



dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality

    implementation(libs.koin.core)
    implementation(compose.desktop.currentOs)
    implementation(projects.app.shared)
    implementation(projects.base)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        jvmArgs(
            "-versionCode=${AppConfig.versionCode}",
            "-versionName=${AppConfig.versionName}",
            "-namespace=${AppConfig.namespace}",
        )

        // 有点 chuck 但没办法
        if (project.gradle.startParameter.taskNames.contains("Release")
            || project.gradle.startParameter.taskNames.contains("release")) {
            jvmArgs(
                "-release=true",
            )
        }



        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = AppConfig.namespace
            packageVersion = AppConfig.versionName

        }
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}
