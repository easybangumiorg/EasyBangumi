// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.1" apply false
    id("com.android.library") version "8.0.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.21" apply false
}

tasks.create<Delete>("clean") {
    delete {
        rootProject.buildDir
    }
}

subprojects {
    // 定义检查依赖变化的时间间隔,!!配置为0实时刷新
    configurations.all {
        // check for updates every build
        resolutionStrategy.cacheChangingModulesFor(0, java.util.concurrent.TimeUnit.SECONDS)
    }
}