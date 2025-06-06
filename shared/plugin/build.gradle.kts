
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.kotlinxAtomicfu)
}
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

    }

    // 提前预埋保证 commonMain 是纯 kotlin 环境
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        val commonMain by getting

        val jvmMain = create("jvmMain") {
            dependsOn(commonMain)
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
        }

        val androidMain by getting {
            dependsOn(jvmMain)
        }

        val iosMain = create("iosMain") {
            dependsOn(commonMain)
        }


        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlin.reflect)
            implementation(libs.koin.core)
            implementation(libs.paging.multiplatform.common)

            implementation(projects.shared.data)
            implementation(projects.shared.resources)

            implementation(projects.lib.logger)
            implementation(projects.lib.unifile)
            implementation(projects.lib.store)
            implementation(projects.lib.utils)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
//            implementation(compose.uiTest)
        }

        jvmMain.dependencies {
            implementation(projects.javascript.rhino)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }

        iosMain.dependencies {

        }
    }
}

android {
    namespace = AppConfig.namespace + ".shared.plugin.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

}


dependencies {

}


