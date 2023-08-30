pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        google()
        maven {
            url = uri("http://4thline.org/m2")
            isAllowInsecureProtocol = true
        }

        maven { url = uri("https://jitpack.io") }


    }
    versionCatalogs {
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
        create("compose") {
            from(files("gradle/compose.versions.toml"))
        }
        create("build") {
            from(files("gradle/build.versions.toml"))
        }
        create("extension") {
            from(files("gradle/extension.versions.toml"))
        }
    }
}

rootProject.name = "EasyBangumi"
include(":app")
//include(":app-old")
include(":easy-crasher")
include(":source:source-utils")
include(":source:source-api")
include(":easy-dlna")
include(":easy-i18n")
include(":extension:extension-api")
include(":extension:extension-core")
include(":injekt")
include(":app-download")
