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

// ----------app----------
includeModule(":app:android", "app/android")
includeModule(":app:desktop", "app/desktop")
includeModule(":app:shared", "app/shared")

// ----------common----------
includeModule(":common:plugin:api", "common/plugin/api")
includeModule(":common:plugin:core", "common/plugin/core")
includeModule(":common:plugin:utils", "common/plugin/utils")
includeModule(":common:plugin:inner", "common/plugin/inner")

includeModule(":common:theme", "common/theme")
includeModule(":common:resources", "common/resources")

includeModule(":common:database", "common/database")

includeModule(":common:foundation", "common/foundation")

includeModule(":base:test", "base/test")

// ----------base----------
// includeModule(":base:compose", "base/compose")
includeModule(":base:model", "base/model")
includeModule(":base:service", "base/service")
includeModule(":base:utils", "base/utils")

// ----------lib----------
includeModule(":lib:inject", "lib/inject")
includeModule(":lib:unifile", "lib/unifile")
includeModule(":lib:javascript", "lib/javascript")




