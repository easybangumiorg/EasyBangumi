import com.squareup.kotlinpoet.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.SourceDirectorySet
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
        description = "The name of the build config file to create. Defaults to BuildConfig.kt."
    )
    abstract val buildConfigFileName: Property<String?>


    @get:Input
    @get:Option(
        option = "sourceSet",
        description = "The source set to generate the BuildConfig file in. Defaults to commonMain."
    )
    abstract val sourceSet: Property<SourceDirectorySet>

    @get:Nested
    lateinit var config: ConfigProperties

    @TaskAction
    fun executeTask() {
        // Retrieve properties or their default values.
        val packageName = packageName.get()
        val buildConfigFileName = buildConfigFileName.orNull ?: "BuildConfig.kt"

        val logger = project.logger

        val kotlinFileBuilder = FileSpec.builder(packageName, buildConfigFileName)
        kotlinFileBuilder.defaultImports.add("kotlin.String")

        val buildConfigObject = TypeSpec.objectBuilder(buildConfigFileName.substringBeforeLast(".kt"))
            .addModifiers(KModifier.PUBLIC)

        config.properties.forEach {
            val prop = PropertySpec
                .builder(it.name, it.clazz)
                .initializer(it.template, it.value)
                .build()
            buildConfigObject.addProperty(prop)
        }

        val  kotlinFile = kotlinFileBuilder.addType(buildConfigObject.build()).build()

        val directory = buildString {
            append("generated")
            appendFileSeparator
            append("source")
            appendFileSeparator
            append("buildConfig")
        }

        val outputDir = project.layout.buildDirectory.dir(directory)

        // Ensure the output directory exists
        outputDir.get().asFile.mkdirs()

        logger.info("Generated BuildConfig file: write to ${outputDir.get().asFile.path}")


        // Write the generated Kotlin file to the output directory
        kotlinFile.writeTo(outputDir.get().asFile)
    }

}

val StringBuilder.appendFileSeparator: StringBuilder
    get() = append(File.separator)