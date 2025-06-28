import org.gradle.api.Project
import org.gradle.internal.extensions.core.extra
import java.util.Properties

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
// 一个项目只会加载一次实例，zh
lateinit var easyConfigProvider: EasyBuildConfigProvider
class EasyBuildConfigProvider(
    private val project: Project
) {

    companion object {
        const val KEY_SHOW_NAMESPACE = "easy.build.showNamespace"
        const val KEY_NAMESPACE = "easy.build.namespace"
        const val KEY_VERSION_NAME = "easy.build.versionName"
        const val KEY_VERSION_CODE = "easy.build.versionCode"

        const val KEY_OPT_MD3_API = "easy.build.optMd3Api"
    }

    init {
        easyConfigProvider = this
    }

    private val localPropertiesFile = project.rootProject.file("local.properties")
    val localProperties: Properties by lazy {
        Properties().apply {
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use(::load)
            }
        }
    }


    val namespace: String = findProperty(KEY_NAMESPACE)
    val showNamespace: String =  project.findProperty(KEY_SHOW_NAMESPACE)?.toString()?: namespace
    val versionName: String = findProperty(KEY_VERSION_NAME)
    val versionCode: Int = findProperty(KEY_VERSION_CODE).toIntOrNull()
        ?: throw IllegalArgumentException("Property '${KEY_VERSION_CODE}' must be an integer.")

    val optMd3Api: Boolean = project.findProperty(KEY_OPT_MD3_API)?.toString()?.toBoolean() ?: false
    init {

        project.extra.set(KEY_NAMESPACE, namespace)
        project.extra.set(KEY_VERSION_NAME, versionName)
        project.extra.set(KEY_VERSION_CODE, versionCode)
        project.extra.set(KEY_SHOW_NAMESPACE, showNamespace)
    }

    fun findProperty(key: String): String {
        return System.getenv(key)
            ?: localProperties.getProperty(key)
            ?: project.findProperty(key)?.toString()
            ?: throw IllegalArgumentException("Property '$key' is required.")


    }


}