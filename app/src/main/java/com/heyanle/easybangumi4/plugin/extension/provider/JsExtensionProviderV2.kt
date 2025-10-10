package com.heyanle.easybangumi4.plugin.extension.provider


import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.copyTo

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
    private val cacheFolder: String,
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

    private val isAssetsDebugLoaded = AtomicBoolean(false)
    private var assetsDebugExtensionInfo: ExtensionInfo? = null

    private fun getAssetsDebugExtensionInfo(): ExtensionInfo? {
        if (isAssetsDebugLoaded.compareAndSet(false, true)) {
            val file = APP.assets.open("extension_test.js").use {
                File(cacheFolder).mkdirs()
                val file = File(cacheFolder, "test.${EXTENSION_SUFFIX}")
                file.delete()
                file.createNewFile()
                file.deleteOnExit()
                file.outputStream().use { output ->
                    it.copyTo(output)
                }
                file
            }
            val loader =  loaderFromFile(file)
            if (loader?.canLoad() == true) {
                assetsDebugExtensionInfo = loader.load()
            }
        }
        return assetsDebugExtensionInfo
    }

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
                        var info = d.map {
                            scope.async(Dispatchers.IO) {
                                loadFromIndex(it)
                            }

                        }.awaitAll()
                        if (BuildConfig.DEBUG) {
                            val debugInfo = getAssetsDebugExtensionInfo()
                            if (debugInfo != null) {
                                info = info.toMutableList()
                                info.add(debugInfo)
                            }
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

    private suspend fun loadFromIndex(indexItem: IndexItem): ExtensionInfo? {
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
        file: List<File>
    ): List<DataResult<Unit>> {
        val update = hashMapOf<String, IndexItem>()
        val res = file.map {
            appendOrUpdate(it, false)
        }.map {
            when (it) {
                is DataResult.Ok -> {
                    update[it.data.key] = it.data
                }
                else -> {}
            }
            it.map { Unit }
        }

        if (update.isNotEmpty()) {
            indexHelper.update {
                it.map {
                    val n = update.remove(it.key)
                    n ?: it
                }
                it + update.values.toList()
            }
        }
        return res
    }

    suspend fun appendOrUpdate(
        file: File,
        update: Boolean = true,
    ): DataResult<IndexItem> {
        return scope.async {
            val loader = loaderFromFile(file)
            if (loader == null || !loader.canLoad()) {
                return@async DataResult.error<IndexItem>("loader can not load $file")
            }

            val info = loader.load()
            if (info == null) {
                return@async DataResult.error<IndexItem>("loader load null")
            }
            if (info is ExtensionInfo.InstallError) {
                return@async DataResult.error<IndexItem>("loader load error: ${info.errMsg}", info.exception)
            }
            val key = info.key
            val suffix = when  {
                file.name.endsWith(JsExtensionProvider.Companion.EXTENSION_CRY_SUFFIX) -> JsExtensionProvider.Companion.EXTENSION_CRY_SUFFIX
                else -> JsExtensionProvider.Companion.EXTENSION_SUFFIX
            }
            val target = File(extensionFolder, "${key}${info.versionName}.${suffix}")
            target.parentFile?.mkdirs()
            val finallyFile = file.copyTo(target, true)
            val indexItem = IndexItem(
                key = key,
                fileName = finallyFile.name,
            )
            if (update) {
                indexHelper.update {
                    var needAppend = true
                    val n = it.map {
                        if (it.key == key) {
                            needAppend = false
                            indexItem
                        } else {
                            it
                        }
                    }
                    if (needAppend) {
                        n + indexItem
                    } else {
                        n
                    }
                }

            }
            DataResult.Ok(indexItem)
        }.await()
    }

    suspend fun delete(
        key: String
    ): DataResult<Unit> {
        return scope.async {
            val item = _flow.value.extensionMap[key] ?: return@async DataResult.error<Unit>("not found")
            val file = File(extensionFolder, item.fileName)
            if (file.exists()) {
                if (!file.delete()) {
                    return@async DataResult.error<Unit>("delete file failed")
                }
            }
            indexHelper.update {
                it.filterNot { it.key == key }
            }
            temp.remove(key)
            DataResult.Ok(Unit)
        }.await()
    }

    override fun release() {
        runCatching {
            scope.cancel()
        }

    }
}