纯纯看番 Module plugin

* 锚定 jdk 版本
* 统一读取配置
* 提供 module 样板

jdk 版本在 `ApplyHelper` 中配置，先暂时硬编码

### 配置

* easy.build.namespace
* easy.build.androidCompileSdk
* easy.build.androidMinSdk
* easy.build.versionName
* easy.build.versionCode

读取顺序

1. `System.getenv()`
2. `local.properties`
3. `gradle.properties`


一般而言 Plugin 会自动装配这些参数，但如果需要在 Build.gradle 中使用配置，可通过 ext 对象读取.

例如在 Desktop app 模块中需要手动获取配置后传入 EasyConfig （EasyBuild 和 EasyConfig 解耦合， EasyConfig 只负责生成配置文件）

```kotlin
plugins {
    alias(builds.plugins.kotlinJvm)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.compose)
    id("EasyConfig")
    id("EasyLibBuild")
}



// EasyLibBuild Plugin 会将数据放到 extra 中
val namespace = extra.get("easy.build.namespace").toString()
val versionCode = extra.get("easy.build.versionCode").toString().toInt()
val versionName = extra.get("easy.build.versionName").toString()


compose.desktop {

    application {

        mainClass = "org.easybangumi.next.MainKt"

        buildTypes.release.proguard {
            optimize.set(false)
            obfuscate.set(false)
            isEnabled.set(false)
        }

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("../assets"))

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = namespace
            packageVersion = versionName

        }
    }
}

easyConfig {
    packageName.set(namespace)
    buildConfigFileName.set("EasyConfig")
    sourceDir.set(kotlin.sourceSets.findByName("main")?.kotlin)


    configProperties {
        "NAMESPACE" with namespace
        "VERSION_CODE" with versionCode
        "VERSION_NAME" with versionName
    }
}

```


### 1. KMP module

```kotlin
plugins {
    alias(builds.plugins.kotlinMultiplatform)
    alias(builds.plugins.androidLibrary)
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

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(projects.lib.store)
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

```


### 2. Android Library module

```kotlin

plugins {
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinAndroid)
    id("EasyLibBuild")
}
dependencies {
    implementation(libs.media3.exoplayer)
    implementation(projects.libplayer.api)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)

}
```

### 3. Jar module

```kotlin
plugins {
    alias(builds.plugins.kotlinJvm)
    id("EasyLibBuild")
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(projects.logger)
    implementation(projects.lib.utils)

}

```