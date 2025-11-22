import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import java.io.File

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

abstract class MakeConfigTask: DefaultTask() {

    init {
        description = "Create a build config file for commonMain"
        group = BasePlugin.BUILD_GROUP
    }


    // Package name for the generated BuildConfig file.
    @get:Input
    @get:Option(
        option = "packageName",
        description = "The package name to set for the project in the build config file."
    )
    abstract val packageName: Property<String>

    // Optional file name for the BuildConfig, defaulting to BuildConfig.kt.
    @get:Input
    @get:Optional
    @get:Option(
        option = "buildConfigFileName",
        description = "The name of the build config object to create. Defaults to BuildConfig."
    )
    abstract val buildConfigName: Property<String?>

    @get:Input
    @get:Optional
    @get:Option(
        option = "needDebugProperties",
        description = "Generated IS_DEBUG property in BuildConfig.kt, default is false."
    )
    abstract val needDebugProperties: Property<Boolean>


    @get:Nested
    lateinit var config: ConfigProperties


//    @get:OutputDirectory
//    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun executeTask() {
        // 先生成了再说
        apply(true)
        apply(false)
    }


    private fun apply(
        isDebug: Boolean
    ){
        // Retrieve properties or their default values.
        val packageName = packageName.get()
        val buildConfigName = this@MakeConfigTask.buildConfigName.orNull ?: "BuildConfig"

        val logger = project.logger



        val stringBuilder = StringBuilder("package $packageName")
        stringBuilder.appendLine()

        val importSet = mutableSetOf<String?>()
        config.properties.forEach {
            importSet.add(it.clazz.qualifiedName)
        }

        importSet.forEach {
            if (it != null) {
                stringBuilder.appendLine("import $it")
            }
        }

        stringBuilder.appendLine()
        stringBuilder.appendLine("object ${buildConfigName} {")


        config.properties.forEach {
            stringBuilder.appendLine("\t" + it.toLine())
        }


        if (needDebugProperties.get()) {
            stringBuilder.appendLine("\tconst val IS_DEBUG = $isDebug")
        }
        stringBuilder.appendLine("}")



        val root = EasyConfigPlugin.getGeneratedPath(
            project,
            if (isDebug) "debug" else "release"
        )
        val dir = File(root, packageName)
        val f = File(dir, "$buildConfigName.kt")
        dir.mkdirs()
        f.delete()
        f.createNewFile()
        f.writeText(stringBuilder.toString())

        println("Generated BuildConfig file at: ${f.absolutePath}")

    }

}

val StringBuilder.appendFileSeparator: StringBuilder
    get() = append(File.separator)