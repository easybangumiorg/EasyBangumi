import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask


plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyConfig")
    id("EasyLibBuild")
}

apply(from = "../build_common/property_loader.gradle.kts")
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
//            configurationFiles.from(project.file("compose-desktop.pro"))
//            optimize.set(true)
//            obfuscate.set(true)
//            isEnabled.set(true)

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

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = applicationId
            packageVersion = versionName

            // ./gradlew:app:desktop:suggestRuntimeModules
            modules(
                "java.instrument",
                "java.management",
                "java.net.http",
                "jcef",
                "jdk.unsupported",
                "jdk.xml.dom",
                "java.base"
            )


            windows {
                iconFile.set(project.rootProject.file("logo/logo.png"))
                menuGroup = "EasyBangumi.org"
                console = false
                dirChooser = true
                menu = true
                shortcut = true
            }
            linux {
                iconFile.set(project.file("logo/logo.png"))
                packageName = "EasyBangumi.org"
            }
            macOS {
                iconFile.set(project.rootProject.file("logo/logo.icns"))
                packageName = "纯纯看番 Next"
                bundleID = applicationId
                dockName = "纯纯看番 Next"
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

tasks.register("printJdkPath") {
    group = "help"
    description = "Print current JDK path and details"

    doFirst {
        val javaHome = System.getProperty("java.home")
        val javaVersion = System.getProperty("java.version")
        val javaVendor = System.getProperty("java.vendor")

        println("=== Current Task JDK Path ===")
        println("java.home: $javaHome")
        println("java.version: $javaVersion")
        println("java.vendor: $javaVendor")
        println("java.vm.name: ${System.getProperty("java.vm.name")}")
    }
}

afterEvaluate {
    // copy native lib for mac
    tasks.named<AbstractJPackageTask>("createReleaseDistributable") {
        val javaHome = System.getProperty("java.home")
        val copyItem = listOf(
            // 1. jcef
            "../Frameworks" to "Contents/runtime/Contents"
        ).map {
            val source = File(javaHome).resolve(it.first).normalize()
            source to it.second
        }

        copyItem.forEach {
            inputs.dir(it.first)
        }

        doLast("copy native lib") {
            val appBundle = destinationDir.get().asFile.walk().find {
                it.name.contains("${applicationId}.app") && it.isDirectory
            }?: throw GradleException("Cannot find ${applicationId}.app in $destinationDir")
            copyItem.forEach { (sourcePath, destPath) ->
                var dest = appBundle.resolve(destPath)?.normalize()
                    ?: throw GradleException("Cannot find ${destPath} in $appBundle")
                ProcessBuilder().run {
                    command("cp", "-r", sourcePath.absolutePath, dest.absolutePath)
                    inheritIO()
                    start()
                }.waitFor().let {
                    if (it != 0) {
                        throw GradleException("Failed to copy $sourcePath")
                    }
                }
                println("Copied $sourcePath to $dest")
            }
        }
    }

//
//    // copy native lib for windows
//    tasks.named<AbstractJLinkTask>("createRuntimeImage") {
//        val javaHome = System.getProperty("java.home")
//        val copyItem = listOf(
//            // 1. jcef
//            "bin/jcef_helper.exe" to "bin/jcef_helper.exe",
//        ).map {
//            val source = File(javaHome).resolve(it.first).normalize()
//            source to it.second
//        }
//        copyItem.forEach {
//            inputs.dir(it.first)
//        }
//
//        doLast("copy native lib") {
//            copyItem.forEach { (source, destPath) ->
//                val dest = destinationDir.file(destPath)
//                source.copyTo(dest.get().asFile)
//            }
//        }
//    }
}

