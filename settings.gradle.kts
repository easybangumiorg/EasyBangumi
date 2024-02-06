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
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        mavenCentral()
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
include(":easy-crasher")
include(":easy-i18n")
include(":injekt")
include(":lib_upnp")
