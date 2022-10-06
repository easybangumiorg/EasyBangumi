pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {url = uri("https://jitpack.io")}
        mavenCentral()
    }
}
rootProject.name = "EasyBangumi"
include (":app")
include(":lib-anim")
include(":easy-lightdark")
include(":easy-media")
include(":easy-crasher")
include(":easy-view")
include(":easy-player")
