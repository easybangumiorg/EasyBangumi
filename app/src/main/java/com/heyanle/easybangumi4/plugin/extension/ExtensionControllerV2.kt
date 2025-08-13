package com.heyanle.easybangumi4.plugin.extension

import android.content.Context
import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProviderV2
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val context: Context,
    val jsExtensionFolder: String,
    private val cacheFolder: String,
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

    fun init() {
        SourceCrashController.onExtensionStart()
        jsExtensionProviderV2.init()
        SourceCrashController.onExtensionEnd()
        scope.launch {
            jsExtensionProviderV2.flow.collectLatest { state ->
                _state.update {
                    it.copy(
                        loading = state.loading,
                        extensionInfoMap = state.extensionMap
                    )
                }
            }
        }
    }

}