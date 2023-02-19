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
    }
}
rootProject.name = "EasyBangumi"
include(":app")
include(":easy-crasher")
include(":source-core")
include(":source-api")
include(":easy-dlna")
include(":easy-i18n")
include(":extension:extension-api")
include(":extension:extension-load")
include(":extension:extension-inner")
