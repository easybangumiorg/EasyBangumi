import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyConfig")
    id("EasyLibBuild")
}



// 暂时先这样解决吧，EasyLibBuild Plugin 会将数据放到 extra 中
val namespace = extra.get("easy.build.namespace").toString()
val applicationId = extra.get("easy.build.applicationId").toString()
val versionCode = extra.get("easy.build.versionCode").toString().toInt()
val versionName = extra.get("easy.build.versionName").toString()


dependencies {
    api(compose.desktop.currentOs)

    api(projects.shared)
    api(projects.logger)
    api(projects.lib)


    api(libs.koin.core)
    api(libs.log4j.core)
    api(libs.log4j.slf4j.impl)

    api(libs.vlcj)
    api(projects.libplayer.libplayerVlcj)

    api(builds.kotlinPoet)
}

compose.desktop {

    application {

        mainClass = "org.easybangumi.next.MainKt"

        buildTypes.release.proguard {
            optimize.set(false)
            obfuscate.set(false)
            isEnabled.set(false)
        }


        jvmArgs.addAll(
            listOf(
//                "-Dsun.java2d.uiScale=1.0",
                "-Dawt.useSystemAAFontSettings=lcd",
                "-Dswing.aatext=true",
                "-Dsun.java2d.metal=true",
            )
        )

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("../assets"))

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = applicationId
            packageVersion = versionName

        }
    }
}

easyConfig {
    packageName.set(namespace)
    buildConfigName.set("EasyConfig")
    sourceDir.set(kotlin.sourceSets.findByName("main")?.kotlin)
    debugProperties.set(true)

    configProperties {
        "NAMESPACE" with applicationId
        "VERSION_CODE" with versionCode
        "VERSION_NAME" with versionName
    }
}
