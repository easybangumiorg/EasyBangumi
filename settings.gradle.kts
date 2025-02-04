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

includeModule(":app:android", "app/android")
includeModule(":app:desktop", "app/desktop")
includeModule(":app:shared", "app/shared")

// ----------repository----------
includeModule(":repository:cartoon", "repository/cartoon")

// ----------common----------
includeModule(":common:plugin:api", "common/plugin/api")
includeModule(":common:plugin:core", "common/plugin/core")
includeModule(":common:plugin:utils", "common/plugin/utils")

includeModule(":common:theme", "common/theme")
includeModule(":common:i18n", "common/i18n")

// ----------lib----------
includeModule(":lib:inject", "lib/inject")
includeModule(":lib:unifile", "lib/unifile")
includeModule(":lib:javascript", "lib/javascript")

// ----------base----------
includeModule(":base:compose", "base/compose")
includeModule(":base:model", "base/model")
includeModule(":base:utils", "base/utils")

