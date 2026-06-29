includeBuild("buildLogic")

rootProject.name = "EasyBangumi"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        maven("https://jogamp.org/deployment/maven")
    }

    versionCatalogs {
        create("builds") {
            from(files("gradle/builds.version.toml"))
        }
        create("libs") {
            from(files("gradle/libs.version.toml"))
        }
    }
}

fun includeModule(modulePath: String, dir: String? = null) {
    include(modulePath)
    if (dir != null) {
        project(modulePath).apply {
            projectDir = file(dir)
        }
    }

}

// 要注意 modulePath 最后一段不能重名

// ------------- app -------------
includeModule(":app:android", "app/android")
includeModule(":app:desktop", "app/desktop")

// ------------- shared -------------
includeModule(":shared", "shared")

// ------------- base -------------
includeModule(":base", "base")

// ------------- webview -------------
includeModule(":webview", "webview")
includeModule(":webview:api", "webview/api")
includeModule(":webview:jcef", "webview/jcef")
includeModule(":webview:webkit", "webview/webkit")

// -- 以下为独立代码层级 ---

// ------------- player -------------
includeModule(":libplayer:libplayer_api", "libplayer/api")
includeModule(":libplayer:libplayer_vlcj", "libplayer/vlcj")
includeModule(":libplayer:libplayer_exoplayer", "libplayer/exoplayer")

// ------------- javascript -------------
includeModule(":javascript:rhino", "javascript/rhino")
includeModule(":javascript:quickjs_kt", "javascript/quickjskt")

// ------------- logger -------------
includeModule(":logger", "logger")



