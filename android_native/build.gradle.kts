// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(build.plugins.android.application) apply false
    alias(build.plugins.android.library) apply false
    alias(build.plugins.kotlin.android) apply false
    alias(build.plugins.kotlin.jvm) apply false

    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}


tasks.create<Delete>("clean") {
    delete {
        rootProject.layout.buildDirectory
    }
}

subprojects {
    // 定义检查依赖变化的时间间隔,!!配置为0实时刷新
    configurations.all {
        // check for updates every build
        resolutionStrategy.cacheChangingModulesFor(0, java.util.concurrent.TimeUnit.SECONDS)
    }
}