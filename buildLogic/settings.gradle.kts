rootProject.name = "EasyBangumiBuilldLogic"
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
            println(files("../gradle/builds.version.toml"))
            from(files("../gradle/builds.version.toml"))
        }
        create("libs") {
            from(files("../gradle/libs.version.toml"))
        }
    }
}

include(":easy_config")
include(":android_app_resource")
