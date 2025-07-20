
plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinxAtomicfu)
    id("EasyLibBuild")
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

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended) // 此依赖需要在生产环境中进行剪枝，非常巨大
            implementation(compose.ui)
            implementation(compose.material3)

            implementation(libs.moko.resources.compose)
            implementation(libs.coil.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.paging.multiplatform.common)
            implementation(libs.paging.multiplatform.compose)
            implementation(libs.md3.window.size)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
//            implementation(libs.kotlinx.atomicfu.gradle)

            api(projects.lib)
//
//            api(projects.shared.plugin)
//            api(projects.shared.plugin.pluginBangumi)

            api(projects.shared.source)
            api(projects.shared.sourceApi)


            api(projects.shared.data)
            api(projects.shared.foundation)
            api(projects.shared.resources)
            api(projects.shared.platform)
            api(projects.shared.scheme)
            api(projects.shared.ktor)


        }

//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//            implementation(compose.uiTest)
//        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.swing)
        }
        iosMain.dependencies {

        }
    }
}


dependencies {
}




