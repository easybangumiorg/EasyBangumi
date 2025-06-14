import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.daemon.common.FileSystem

plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyConfig")
    id("EasyLibBuild")
}



// 暂时先这样解决吧，EasyLibBuild Plugin 会将数据放到 extra 中
val showNamespace = extra.get("easy.build.showNamespace").toString()
val namespace = extra.get("easy.build.namespace").toString()
val versionCode = extra.get("easy.build.versionCode").toString().toInt()
val versionName = extra.get("easy.build.versionName").toString()


dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview funx tionality


    implementation(compose.desktop.currentOs)

    implementation(projects.shared)
    implementation(projects.logger)

    implementation(libs.koin.core)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j.impl)

    implementation(libs.vlcj)
    implementation(projects.libplayer.vlcj)

    implementation(builds.kotlinPoet)
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
            packageName = namespace
            packageVersion = versionName

        }
    }
}

easyConfig {
    packageName.set(showNamespace)
    buildConfigName.set("EasyConfig")
    sourceDir.set(kotlin.sourceSets.findByName("main")?.kotlin)


    configProperties {
        "NAMESPACE" with namespace
        "VERSION_CODE" with versionCode
        "VERSION_NAME" with versionName
    }
}
