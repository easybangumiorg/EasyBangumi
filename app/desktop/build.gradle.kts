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

val easyBangumiPackageName = "纯纯看番Next"

dependencies {
    api(compose.desktop.currentOs)

    api(projects.shared)
    api(projects.logger)
    api(projects.lib)


    api(libs.koin.core)
    api(libs.log4j.core)
    api(libs.log4j.slf4j.impl)

    api(libs.vlcj)
    api(libs.navigation.compose)
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
                "-Dsun.java2d.uiScale=auto",
                "-Dsun.java2d.dpiaware=true",
                "-Dawt.useSystemAAFontSettings=lcd",
                "-Dswing.aatext=true",
                "-Dsun.java2d.metal=true",
                "--add-exports=java.base/java.lang=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                "-Djogamp.debug.JNILibLoader=true",
                "-Dprism.allowhidpi=true",
                "-Dcompose.interop.blending=true",
                "-Dcompose.swing.render.on.graphics=true"
            )
        )

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("../assets"))

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = easyBangumiPackageName
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
                iconFile.set(project.rootProject.file("logo/logo.ico"))
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

val osName = System.getProperty("os.name").lowercase()

var isMac = "mac" in osName || "os x" in osName || "darwin" in osName
var isWindows = "windows" in osName
var isLinux = "linux" in osName

// 如果是 packageReleaseDmg task 启动，也认为是 mac 平台
fun maybeMac(): Boolean {
    val isReleaseDmg = gradle.startParameter.taskNames.any {
        val nameL = it.lowercase()
        nameL.contains("release") && nameL.contains("dmg")
    }
    return isReleaseDmg || isMac
}

// 如果是 packageReleaseMsi task 启动，也认为是 windows 平台
fun maybeWindows(): Boolean {
    val isReleaseMsi = gradle.startParameter.taskNames.any {
        val nameL = it.lowercase()
        nameL.contains("release") && nameL.contains("msi")
    }
    return isReleaseMsi || isWindows
}


afterEvaluate {
    if (maybeMac()) {
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
                    // 应该找 纯纯看番Next.app 但因为包含中文怕有兼容性问题
                    it.name.endsWith(".app") && it.isDirectory
                }?: throw GradleException("Cannot find .app in ${destinationDir.get().asFile}")
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
    }



    // copy native lib for windows
    if (maybeWindows()) {
        tasks.named<AbstractJLinkTask>("createRuntimeImage") {
            val javaHome = System.getProperty("java.home")
            val copyItem = listOf(
                // 1. jcef
                "bin/jcef_helper.exe" to "bin/jcef_helper.exe",
                "bin/cef_server.exe" to "bin/cef_server.exe",
                "bin/icudtl.dat" to "bin/icudtl.dat",
                "bin/v8_context_snapshot.bin" to "bin/v8_context_snapshot.bin",
            ).map {
                val source = File(javaHome).resolve(it.first).normalize()
                source to it.second
            }
            copyItem.forEach {
                inputs.file(it.first)
            }

            doLast("copy native lib") {
                copyItem.forEach { (source, destPath) ->
                    val dest = destinationDir.file(destPath)
                    val destFile = dest.get().asFile
                    source.copyTo(dest.get().asFile)
                    println("copied $source to $destFile")
                }

            }
        }

    }
}
