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

// ------------- app -------------
includeModule(":app:android", "app/android")
includeModule(":app:desktop", "app/desktop")

// ------------- shared -------------
includeModule(":shared", "shared")
includeModule(":shared:platform", "shared/platform")
includeModule(":shared:resources", "shared/resources")
includeModule(":shared:foundation", "shared/foundation")
includeModule(":shared:theme", "shared/theme")
includeModule(":shared:data", "shared/data")
includeModule(":shared:preference", "shared/preference")
includeModule(":shared:scheme", "shared/scheme")
includeModule(":shared:debug", "shared/debug")
includeModule(":shared:playcon", "shared/playcon")
includeModule(":shared:ktor", "shared/ktor")

includeModule(":shared:plugin", "shared/plugin")
includeModule(":shared:plugin:bangumi", "shared/plugin/bangumi")


// ------------- lib -------------
includeModule(":lib", "lib")
includeModule(":lib:utils", "lib/utils")
includeModule(":lib:store", "lib/store")
includeModule(":lib:unifile", "lib/unifile")
includeModule(":lib:serialization", "lib/serialization")


// -- 以下为独立代码层级 ---

// ------------- player -------------
includeModule(":libplayer:api", "libplayer/api")
includeModule(":libplayer:vlcj", "libplayer/vlcj")
includeModule(":libplayer:exoplayer", "libplayer/exoplayer")

// ------------- javascript -------------
includeModule(":javascript:rhino", "javascript/rhino")

// ------------- logger -------------
includeModule(":logger", "logger")



