rootProject.name = "EasyBangumiBuildSrc"

dependencyResolutionManagement {


    versionCatalogs {
        create("builds") {
            from(files("../gradle/builds.version.toml"))
        }
        create("libs") {
            from(files("../gradle/libs.version.toml"))
        }
    }
}