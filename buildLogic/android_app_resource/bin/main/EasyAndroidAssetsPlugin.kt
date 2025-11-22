
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.tasks.MergeArtProfileTask
import com.android.build.gradle.tasks.MergeResources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

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
class EasyAndroidAssetsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create(EXTENSION_NAME, EasyAndroidAssetsPluginExtension::class, target)


        target.afterEvaluate {
            val commonInput = extension.assetsDir.get().dir("common").asFile
            val androidInput = extension.assetsDir.get().dir("android").asFile
            // 将配置路径/common 和/android 添加到 Android 的 assets 目录中
            target.pluginManager.withPlugin("com.android.application") {
                target.extensions.configure<BaseAppModuleExtension> {
                    sourceSets.named("main") {
                        assets.srcDir(commonInput)
                        assets.srcDir(androidInput)
                    }
                }
            }
        }
    }

    companion object {
        // Names for the extension and the task provided by this plugin.
        const val EXTENSION_NAME = "easyAndroidAssets"
    }

}