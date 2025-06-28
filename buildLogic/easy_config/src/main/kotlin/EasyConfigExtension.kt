import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

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

abstract class EasyConfigExtension(project: Project) {

    @Inject
    private val objects = project.objects

    val buildConfigName = objects.property(String::class)

    // Property for specifying the package name to be used in the generated BuildConfig file.
    val packageName = objects.property(String::class)

    // Property for specifying the source set to generate the BuildConfig file in.
    val sourceDir = objects.property(SourceDirectorySet::class)

    val debugProperties = objects.property<Boolean>()

    @Internal
    lateinit var config: ConfigProperties

    fun configProperties(action: Action<ConfigPropertiesBuilder>) {
        val builder = ConfigPropertiesBuilder {
            action.execute(this)
        }
        config = ConfigProperties(builder.allConfigProperties)
    }

}