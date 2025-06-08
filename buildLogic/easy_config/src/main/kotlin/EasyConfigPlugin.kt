import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

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

abstract class EasyConfigPlugin: Plugin<Project> {


    // TODO 依赖其他 task
    override fun apply(target: Project) {
        val extension = target.extensions.create(EXTENSION_NAME, EasyConfigExtension::class, target)

        val task =
            target.tasks.register(MAKE_EASY_BUILD_CONFIG, MakeConfigTask::class.java) {
                group = "easyBuildConfig" // Set the task group for organization.
                description = "Generates BuildConfig files for the project." // Set the task description.

                packageName.set(extension.packageName) // Set the package name from the extension.
                buildConfigFileName.set(extension.buildConfigFileName) // Set the build config file name from the extension, if specified.
                config = extension.config // Set the config object from the extension.
                sourceSet.set(extension.sourceDir) // Set the source set from the extension.
            }

        setupGradleSync(target)

        target.afterEvaluate {
            val isRelease = target.gradle.startParameter.taskNames.any {
                it.startsWith("packageRelease") || it.startsWith("assembleRelease")
            }
            val taskToAdd = target.tasks.register<Task>("addToSourceSet") {
                group = "easyBuildConfig"
                description = "Adds the generated source directory to the source set for ${if (isRelease) "release" else "debug"} build type."
                extension.sourceDir.get().srcDirs(getGeneratedPath(target, if (isRelease) "release" else "debug"))
            }

            task.get().finalizedBy(taskToAdd)
        }

    }

    private fun setupGradleSync(project: Project) {
        val tasks: TaskCollection<MakeConfigTask> = project.tasks.withType()
        project.tasks.matching {
            it.name == "prepareKotlinIdeaImport" ||
                    it.name.startsWith("compileKotlin")
        }.configureEach {
            dependsOn(tasks)
        }
    }


    companion object {
        // Names for the extension and the task provided by this plugin.
        const val EXTENSION_NAME = "easyBuildConfig"
        const val MAKE_EASY_BUILD_CONFIG = "makeEasyBuildConfig"


        fun getGeneratedPath(
            target: Project,
            buildType: String = "debug",
        ) = buildString {
            append(target.layout.buildDirectory.asFile.get().absolutePath)
            appendFileSeparator
            append("generated")
            appendFileSeparator
            append("source")
            appendFileSeparator
            append("buildConfig")
            appendFileSeparator
            append(buildType)
        }
    }
}