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
        maven {url = uri("https://jitpack.io")}
        mavenCentral()
    }
}
rootProject.name = "EasyBangumi"
include (":app")
include (":easy-crasher")
include(":lib-anim")
include(":easy-source")
