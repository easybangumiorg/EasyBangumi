import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    alias(builds.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.moshi)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended) // 此依赖需要在生产环境中进行剪枝，非常巨大
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.moshi)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(projects.lib.inject)
            implementation(projects.lib.unifile)

            implementation(projects.base.model)

            implementation(projects.base.service)
            implementation(projects.base.utils)
            implementation(projects.base.compose)

            implementation(projects.common.plugin.api)
            implementation(projects.common.plugin.core)
            implementation(projects.common.plugin.utils)

            implementation(projects.common.database)

            implementation(projects.common.resources)
        }
        desktopMain.dependencies {
            implementation(libs.moshi)
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}




dependencies {

}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

val kspMetaDataList = listOf(
    "kspCommonMainMetadata",
    "kspAndroid",
    "kspDesktop",
)

dependencies {
    kspMetaDataList.forEach {
        add(it, libs.androidx.room.compiler)
    }
}





