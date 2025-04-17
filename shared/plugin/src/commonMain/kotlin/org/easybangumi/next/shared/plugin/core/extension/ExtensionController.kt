package org.easybangumi.next.shared.plugin.core.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.plugin.api.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.core.safe.ExtensionSafeMode
import org.easybangumi.next.shared.plugin.core.extension.loader.JsFileCryExtensionLoader
import org.easybangumi.next.shared.plugin.core.extension.loader.JsFileExtensionLoader
import org.easybangumi.next.shared.plugin.core.extension.loader.JsPkgExtensionLoader
import org.easybangumi.next.shared.plugin.core.extension.provider.ExtensionProvider
import org.easybangumi.next.shared.plugin.core.extension.provider.ProviderFactory
import org.easybangumi.next.shared.plugin.core.info.ExtensionInfo

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
 * ExtensionProvider1 ↘   拓展管理页面用     loader       源加载用
 * ExtensionProvider2 → ExtensionManifest --------> ExtensionInfo
 * ExtensionProvider3 ↗
 */
class ExtensionController {

    private val logger = logger()

    val workerFile = pathProvider.getFilePath("extension")
    val cacheFile = pathProvider.getCachePath("extension")

    // == 状态 =================================================================================

    private val _manifestState = MutableStateFlow<DataState<Map<String, ExtensionManifest>>>(DataState.Companion.loading())
    val manifestState: StateFlow<DataState<Map<String, ExtensionManifest>>> = _manifestState

    private val _infoState = MutableStateFlow<DataState<Map<String, ExtensionInfo>>>(DataState.Companion.loading())
    val infoState: StateFlow<DataState<Map<String, ExtensionInfo>>> = _infoState

    private val singleDispatcher = coroutineProvider.newSingle(this.toString())
    private val scope = CoroutineScope(SupervisorJob() + coroutineProvider.io())

    // == ExtensionProvider ================================================================================

    private val providerMap: Map<Int, ExtensionProvider> = run {

        if (ExtensionSafeMode.isSafeMode()) {
            logger.error("ExtensionSafeMode is enabled")
            _manifestState.update {
                DataState.Companion.error("安全模式，请排查问题插件后重启")
            }
            return@run emptyMap()
        }

        val map = hashMapOf<Int, ExtensionProvider>()
        val result = ProviderFactory.createProvider(
            workerFile,
            cacheFile,
            scope,
            singleDispatcher
        )
        val providerList = result.provider

        if (providerList.isEmpty()) {
            logger.error("providerList is empty")
            _manifestState.update {
                DataState.Companion.error("providerList is empty", result.exception.firstOrNull())
            }
            return@run emptyMap()

        } else {
            providerList.forEach {
                map[it.type] = it
            }
        }
        return@run map.toMap()
    }

    // == loader ==================================================================================

    private val jsLoader = JsFileExtensionLoader()
    private val jsCryLoader = JsFileCryExtensionLoader()
    private val pkgLoader = JsPkgExtensionLoader(jsLoader, jsCryLoader)

    private val loaderMap = mapOf(
        jsLoader.loadType() to jsLoader,
        jsCryLoader.loadType() to jsCryLoader,
        pkgLoader.loadType() to pkgLoader,
    )

    // == flow ==================================================================================

    private var isFirstLoading = true

    init {
        // provider -> manifestState
        scope.launch {
            combine(
                providerMap.values.map { it.flow }
            ) {
                val loadingCount = it.count {
                    it.isLoading() || it.isNone()
                }
                if (isFirstLoading && loadingCount == 0) {
                    isFirstLoading = false
                }

                val loading = isFirstLoading || (loadingCount == it.size)
                if (loading) {
                    return@combine DataState.Companion.loading<Map<String, ExtensionManifest>>()
                }

                val res = hashMapOf<String, ExtensionManifest>()

                val errorState = it.filter { it.isError() }.filterIsInstance<DataState.Error<List<ExtensionManifest>>>()

                if (errorState.size == it.size) {
                    return@combine DataState.Companion.error<Map<String, ExtensionManifest>>(
                        "provider all error ${
                        buildString {
                            errorState.forEach {
                                append(it.errorMsg)
                                append("\n")
                            }
                        }
                    }")
                }

                errorState.forEach {
                    logger.error(it.errorMsg, it.throwable)
                }

                it.filterIsInstance<DataState.Ok<List<ExtensionManifest>>>().forEach {
                    it.data.forEach {
                        res.put(it.key, it)
                    }
                }
                return@combine DataState.Companion.ok(res.toMap())
            }.collectLatest {
                _manifestState.update { it }
            }
        }

        // manifestState -> infoState
        scope.launch {
            manifestState.collectLatest {
                val res = it.map {
                    it.map {
                        val loader = loaderMap[it.value.loadType]
                        loader?.load(it.value)
                    }.filterNotNull().associateBy { it.manifest().key }
                }
                _infoState.update { res }
            }
        }

        refreshExtension()
    }

    // == api ==================================================================================

    fun refreshExtension() {
        scope.launch {
            providerMap.forEach {
                it.value.refresh()
            }
        }
    }

    fun install(
        file: List<UFD>,
        providerType: Int,
        override: Boolean,
        callback: (List<DataState<ExtensionManifest>>) -> Unit
    ) {
        val provider = providerMap[providerType]
        if (provider == null) {
            callback(listOf(DataState.Companion.error("unknown provider type $providerType")))
            return
        }
        provider.install(file, override, callback)
    }

    fun uninstall(
        extensionManifestList: List<ExtensionManifest>,
    ) {
        scope.launch {
            val map = extensionManifestList.groupBy { it.providerType }

            map.map {
                val providerType = it.key
                val extensionManifestList = it.value
                val provider = providerMap[providerType]
                provider?.uninstall(extensionManifestList)
            }
        }

    }
}