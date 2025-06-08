 生成 kotlin Multiplatform 以及纯 Compose Desktop 项目可用的 BuildConfig 文件  
 
虽然理论上支持 Android 平台但实际上纯纯看番当前只有 Desktop 平台会使用，安卓平台依旧使用自带 BuildConfig 传参

## 配置：

```kotlin
// build.gradle.kts

plugins {
    id("EasyConfig")
}

easyBuildConfig {
    packageName.set(AppConfig.namespace)
    buildConfigFileName.set("EasyConfig")
    // 纯 kotlin 项目为 main 
    // KMP 项目为 commonMain
    sourceDir.set(kotlin.sourceSets.findByName("main")!!.kotlin)
    configProperties {
        "NAMESPACE" with AppConfig.namespace
        "VERSION_CODE" with AppConfig.versionCode
        "VERSION_NAME" with AppConfig.versionName
    }
}
```

## 生成类，其中 IS_DEBUG 为自动生成。

```kotlin
package org.easybangumi.next

import kotlin.Boolean
import kotlin.Int
import kotlin.String

public object EasyConfig {
    public val NAMESPACE: String = "org.easybangumi.next"
    
    public val VERSION_CODE: Int = 1
    
    public val VERSION_NAME: String = "1.0.0"
    
    public val IS_DEBUG: Boolean = true
}


```

## MakeEasyConfigTask

该 Task 会生成两份文件，分别位于以下目录：

* `build/generated/source/buildConfig/debug/[package]/[fileName]` 
* `build/generated/source/buildConfig/release/[package]/[fileName]`

其中 `debug` 和 `release` 分别对应 IS_DEBUG 变量为 true 或者 false。

该任务被满足以下条件的任务依赖：

```kotlin
 project.tasks.matching {
    it.name == "prepareKotlinIdeaImport" ||
            it.name.startsWith("compileKotlin")
}.configureEach {
    dependsOn(MakeEasyConfigTask)
}
```

## addToSourceSet

该 Task 会根据当前的构建类型将 MakeEasyConfigTask 生成的文件添加到对应的 sourceSet 中。  

该 Task 在 MakeEasyConfigTask 之后执行。  

获取构建类型逻辑：

```kotlin
val isRelease = target.gradle.startParameter.taskNames.any {
    it.startsWith("packageRelease") || it.startsWith("assembleRelease")
}
```  

也就是如果要 以 Release 构建类型打包，则必须运行 packageReleaseXX 或者 assembleRelease Task。  
以其它 Task 运行的一律视为 Debug 构建类型。

