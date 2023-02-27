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
        google()
        maven {
            url = uri("http://4thline.org/m2")
            isAllowInsecureProtocol = true
        }
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}

rootProject.name = "EasyBangumi"
include(":app")
include(":app-old")
include(":easy-crasher")
include(":source:source-utils")
include(":source:source-api")
include(":easy-dlna")
include(":easy-i18n")
include(":extension:extension-api")
include(":extension:extension-core")
