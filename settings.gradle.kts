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
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
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
includeBuild("third_party/m3u8-ad-audio-probe") {
    dependencySubstitution {
        substitute(module("io.github.0o755:ad-audio-probe-runtime")).using(project(":probe-runtime"))
        substitute(module("io.github.0o755:ad-audio-probe-media3-1.9.2")).using(project(":probe-media3-1-9"))
    }
}
include(":app")
include(":baselineprofile")
include(":easy-crasher")
include(":easy-i18n")
include(":inject")
include(":lib_upnp")

include(":easy-player2")
include(":easy_transformer")


