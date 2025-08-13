package com.heyanle.easybangumi4.plugin.extension.provider

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

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
class JsExtensionProviderV2(
    jsonFileProvider: JsonFileProvider,
    private val extensionFolder: String,
    private val dispatcher: CoroutineDispatcher,
    private val jsRuntime: JSRuntimeProvider
): ExtensionProvider {

    companion object {
        // 扩展名
        const val EXTENSION_SUFFIX = "ebg.js"

        // 加密后的后缀
        const val EXTENSION_CRY_SUFFIX = "ebg.jsc"
    }

    private val temp = ConcurrentHashMap<String, Pair<Long, ExtensionInfo.Installed>>()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    data class IndexItem(
        val key: String,
        val fileName: String,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IndexItem

            if (key != other.key) return false
            if (fileName != other.fileName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + fileName.hashCode()
            return result
        }
    }

    private val indexHelper = jsonFileProvider.extensionIndex
    private val _flow = MutableStateFlow(ExtensionProvider.ExtensionProviderState(true))
    override val flow: StateFlow<ExtensionProvider.ExtensionProviderState> = _flow

    override fun init() {
        scope.launch {
            indexHelper.flow.collectLatest {
                when (it) {
                    is DataResult.Loading -> {
                        _flow.update {
                            it.copy(loading = true)
                        }
                    }
                    is DataResult.Ok -> {
                        val d = it.data
                        val info = d.map {
                            loadFromIndex(it)
                        }
                        val map = info.filterNotNull().associateBy { it.key }
                        _flow.update {
                            it.copy(loading = false, extensionMap = map)
                        }

                    }
                    is DataResult.Error -> {
                        _flow.update {
                            it.copy(loading = false)
                        }
                    }
                }
            }
        }
    }

    private fun loaderFromFile(file: File): ExtensionLoader? {
        if (!file.exists()) {
            return null
        }

        return ExtensionLoaderFactory.getFileJsExtensionLoader(file, jsRuntime)
    }

    private fun loadFromIndex(indexItem: IndexItem): ExtensionInfo? {
        val file = File(extensionFolder, indexItem.fileName)
        if (!file.exists()) {
            return null
        }
        val t = temp[indexItem.key]
        if (t != null && t.first == file.lastModified()) {
            return t.second
        }
        temp.remove(indexItem.key)
        val loader = loaderFromFile(file)
        if (loader == null || !loader.canLoad()) {
            return null
        }
        val info = loader.load() ?: return null
        if (info is ExtensionInfo.Installed) {
            temp[indexItem.key] = Pair(file.lastModified(), info)
        }
        return info
    }

    suspend fun appendOrUpdate(
        file: File,
    ): DataResult<Unit> {
        return scope.async {
            val loader = loaderFromFile(file)
            if (loader == null || !loader.canLoad()) {
                return@async DataResult.error<Unit>("loader can not load")
            }

            val info = loader.load()
            if (info == null) {
                return@async DataResult.error<Unit>("loader load null")
            }
            if (info is ExtensionInfo.InstallError) {
                return@async DataResult.error<Unit>("loader load error: ${info.errMsg}", info.exception)
            }
            val key = info.key
            val suffix = when  {
                file.name.endsWith(JsExtensionProvider.Companion.EXTENSION_CRY_SUFFIX) -> JsExtensionProvider.Companion.EXTENSION_CRY_SUFFIX
                else -> JsExtensionProvider.Companion.EXTENSION_SUFFIX
            }
            val target = File(extensionFolder, "${key}.${suffix}")
            target.parentFile?.mkdirs()
            val finallyFile = file.copyTo(target)
            val indexItem = IndexItem(
                key = key,
                fileName = finallyFile.name,
            )
            indexHelper.update {
                it + indexItem
            }

            DataResult.Ok(Unit)
        }.await()
    }

    override fun release() {
        runCatching {
            scope.cancel()
        }

    }
}