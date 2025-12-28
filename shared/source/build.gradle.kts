
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(libs.plugins.kotlinxAtomicfu)
    alias(libs.plugins.kotlinxSerialization)
    id("EasyLibBuild")
}
kotlin {
    sourceSets {

        val commonMain by getting
        val commonTest by getting
        val jvmMain by getting
        val desktopMain by getting
        val desktopTest by getting
        val androidMain by getting
        val iosMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        commonMain.dependencies {
            implementation(libs.coil.ktor3)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ksoup)

            implementation(projects.shared.platform)
            implementation(projects.shared.ktor)
            implementation(projects.shared.data)
            implementation(projects.shared.resources)
            implementation(projects.logger)
            api(projects.shared.sourceApi)
            api(projects.shared.sourceBangumi)
            api(projects.shared.sourceInner)
            api(projects.lib)

            implementation(projects.javascript.quickjsKt)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
            // 断言库
            implementation("org.jetbrains.kotlin:kotlin-test")
        }


        jvmMain.dependencies {
            implementation(projects.javascript.rhino)
        }

        androidMain.dependencies {

        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
        }
        desktopTest.dependencies {
            implementation(kotlin("test"))
//            implementation(kotlin("test-junit5"))
        }
        iosMain.dependencies {

        }
    }
}

dependencies {

}

// 配置测试任务
tasks.register<Test>("jvmTest") {
    useJUnitPlatform()
}

tasks.register<Test>("androidUnitTest") {
    // Android 单元测试配置
}


