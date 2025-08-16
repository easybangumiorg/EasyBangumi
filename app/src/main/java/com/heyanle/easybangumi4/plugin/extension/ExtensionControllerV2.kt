package com.heyanle.easybangumi4.plugin.extension

import android.content.Context
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProviderV2
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.utils.logi
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
 *
 *  1. 只保留 js 源
 *  2. 去除 file observer 相关的一系列逻辑，改为纯监听 index 文件列表
 */
class ExtensionControllerV2(
    val jsExtensionFolder: String,
    val jsExtensionCacheFolder: String,
    private val jsonFileProvider: JsonFileProvider,
): IExtensionController {

    companion object {
        private const val TAG = "ExtensionControllerV2"
    }

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow<IExtensionController.ExtensionState>(
        IExtensionController.ExtensionState()
    )
    override val state = _state.asStateFlow()

    private val jsRuntimeProvider = JSRuntimeProvider(2)
    private val jsExtensionProviderV2: JsExtensionProviderV2 =
        JsExtensionProviderV2(
            jsonFileProvider,
            jsExtensionFolder,
            dispatcher,
            jsRuntimeProvider,
        )

    override fun init() {
        SourceCrashController.onExtensionStart()
        jsExtensionProviderV2.init()
        SourceCrashController.onExtensionEnd()
        scope.launch {
            jsExtensionProviderV2.flow.collectLatest { state ->
                state.logi(TAG)
                _state.update {
                    it.copy(
                        loading = state.loading,
                        extensionInfoMap = state.extensionMap
                    )
                }
            }
        }
    }

    suspend fun appendOrUpdateExtension(
        uniFile: UniFile
    ): DataResult<JsExtensionProviderV2.IndexItem> {
        return scope.async {
            val file = File(jsExtensionCacheFolder, uniFile.name?:"")
            file.delete()
            file.createNewFile()
            uniFile.openInputStream().copyTo(file.outputStream())
            if (!file.exists()) {
                return@async DataResult.error(
                    "Failed to copy UniFile to local file: ${file.absolutePath}"
                )
            }

            // 根据 Mask 判断是否是加密
            val buffer = ByteArray(JSExtensionCryLoader.FIRST_LINE_MARK.size)

            val size = file.inputStream().use {
                it.read(buffer)
            }
            val suffix = if (size == JSExtensionCryLoader.FIRST_LINE_MARK.size && buffer.contentEquals(JSExtensionCryLoader.FIRST_LINE_MARK)) {
                JsExtensionProviderV2.EXTENSION_CRY_SUFFIX
            } else {
                JsExtensionProviderV2.EXTENSION_SUFFIX
            }
            // 添加后缀
            val targetFile = File(jsExtensionCacheFolder, "${file.name}.$suffix")
            file.renameTo(targetFile)
            return@async jsExtensionProviderV2.appendOrUpdate(targetFile, true)
        }.await()

    }

    suspend fun appendOrUpdateExtension(
        file: File
    ): DataResult<JsExtensionProviderV2.IndexItem> {
        return jsExtensionProviderV2.appendOrUpdate(file)
    }

    suspend fun appendOrUpdateExtension(
        fileList: List<File>
    ): List<DataResult<Unit>> {
        return jsExtensionProviderV2.appendOrUpdate(fileList)
    }

    suspend fun deleteExtension(
        key: String
    ): DataResult<Unit> {
        return jsExtensionProviderV2.delete(key)
    }
}