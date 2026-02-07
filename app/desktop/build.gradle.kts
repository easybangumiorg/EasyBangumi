import org.jetbrains.compose.desktop.application.dsl.TargetFormat

apply(from = "../build_common/easy_build_config.gradle.kts")


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
                "--add-exports=java.base/java.lang=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                "-Dsun.java2d.metal=true",
                "-Djogamp.debug.JNILibLoader=true",
//                "-Dcompose.interop.blending=true"
            )
        )

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("../assets"))

            // 关键配置：禁用资源压缩和优化
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = applicationId
            packageVersion = versionName

            windows {
                iconFile.set(project.file("./src/main/resources/logo.png"))
                menuGroup = "EasyBangumi.org"
                console = false
                dirChooser = true
                menu = true
                shortcut = true
            }
            linux {
                iconFile.set(project.file("./src/main/resources/logo.png"))
                packageName = "EasyBangumi.org"
            }
            macOS {
                iconFile.set(project.file("./src/main/resources/logo.png"))
                bundleID = "org.easybangumi.next"
                dockName = "纯纯看番"
            }


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
        "BANGUMI_APP_ID" with extra["bangumiAppId"] as String
        "BANGUMI_APP_SECRET" with extra["bangumiAppSecret"] as String
        "BANGUMI_APP_CALLBACK_URL" with extra["bangumiAppCallbackUrl"] as String
    }
}
