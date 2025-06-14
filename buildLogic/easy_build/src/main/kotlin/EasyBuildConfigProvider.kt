import org.gradle.api.Project
import org.gradle.internal.extensions.core.extra

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
class EasyBuildConfigProvider(
    private val project: Project
) {

    companion object {
        const val KEY_SHOW_NAMESPACE = "easy.build.showNamespace"
        const val KEY_NAMESPACE = "easy.build.namespace"
        const val KEY_VERSION_NAME = "easy.build.versionName"
        const val KEY_VERSION_CODE = "easy.build.versionCode"
    }


    val namespace: String = findProperty(KEY_NAMESPACE, project)
    val showNamespace: String =  project.findProperty(KEY_SHOW_NAMESPACE)?.toString()?: namespace
    val versionName: String = findProperty(KEY_VERSION_NAME, project)
    val versionCode: Int = findProperty(KEY_VERSION_CODE, project).toIntOrNull()
        ?: throw IllegalArgumentException("Property '${KEY_VERSION_CODE}' must be an integer.")

    init {

        project.extra.set(KEY_NAMESPACE, namespace)
        project.extra.set(KEY_VERSION_NAME, versionName)
        project.extra.set(KEY_VERSION_CODE, versionCode)
    }

    fun findProperty(key: String, project: Project): String {
        return System.getenv(key) ?: run {
            project.findProperty(key)?.toString()
                ?: throw IllegalArgumentException("Property '$key' is required.")
        }

    }


}