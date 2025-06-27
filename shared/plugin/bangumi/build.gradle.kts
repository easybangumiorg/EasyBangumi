plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    id("EasyLibBuild")
    id("EasyConfig")
}
kotlin {
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
        val desktopMain by getting
        val androidMain by getting
        val iosMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        commonMain.dependencies {
            implementation(libs.coil.ktor3)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {

        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}

val showNamespace = extra.get("easy.build.showNamespace").toString()
easyConfig {
    packageName.set(showNamespace)
    buildConfigName.set("BangumiConfig")
    sourceDir.set(kotlin.sourceSets.findByName("commonMain")?.kotlin)

    configProperties {
        "BANGUMI_ACCESS_TOKEN" with getEasyProperty("bangumi.access.token")
    }
}

