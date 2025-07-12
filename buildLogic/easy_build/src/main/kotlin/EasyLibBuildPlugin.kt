
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class EasyLibBuildPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val provider = EasyBuildConfigProvider(target)



        target.group = provider.applicationId
        if (target.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            ApplyHelper.applyKmp(target, provider)
        }

        // 这两种是互斥
        if (target.pluginManager.hasPlugin("com.android.application")) {
            ApplyHelper.applyAndroidApplication(target, provider)
        } else if (target.pluginManager.hasPlugin("com.android.library")) {
            ApplyHelper.applyAndroidLibrary(target, provider)
        }


        if (target.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) {
            ApplyHelper.applyKotlinJar(target, provider)
        }

    }



    companion object {
        internal const val EXT_MODULE_NAME = "moduleName"
    }

}