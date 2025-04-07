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

fun includeModule(moduleName: String, dir: String? = null) {
    include(moduleName)
    if (dir != null) {
        project(moduleName).projectDir = file(dir)
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

// ------------- lib -------------
includeModule(":lib:logger", "lib/logger")
includeModule(":lib:utils", "lib/utils")
includeModule(":lib:store", "lib/store")
includeModule(":lib:unifile", "lib/unifile")




