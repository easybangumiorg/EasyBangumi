
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

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
abstract class PreprocessAssetsTask : DefaultTask() {

    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun process() {
        val output = outputDir.get().asFile
        val commonInput = inputDir.get().dir("common").asFile
        val androidInput = inputDir.get().dir("android").asFile
        project.logger.lifecycle("🚀 开始拷贝 Android 资源: ${commonInput} ${androidInput}")

        if (commonInput.exists()) {
            commonInput.copyRecursively(output, true)
        }
        if (androidInput.exists()) {
            androidInput.copyRecursively(output, true)
        }
    }

}