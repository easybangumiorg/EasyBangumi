package org.easybangumi.next.shared.source.plugin

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okio.buffer
import okio.use
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.SourceProvider
import org.easybangumi.next.shared.source.api.source.SourceConfig
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.api.source.SourceManifest
import org.easybangumi.next.shared.source.api.source.SourceType
import org.easybangumi.next.shared.source.core.source.SourceConfigController
import org.easybangumi.next.shared.source.utils.PluginPathProvider

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

class PluginSourceController(
    val scope: CoroutineScope,
    val sourceConfigController: SourceConfigController,
    val loaderFactory: PluginLoader.Factory,
): SourceProvider {

    companion object {
        const val INDEX_FILE_NAME = "index"
        const val FILE_SUFFIX = "js"
    }

    private val logger = logger()

    private val workPath = PluginPathProvider.getPluginWorkPath()
    private val workFile = UniFileFactory.fromUFD(workPath)
    private val workFileOpt = workFile ?: throw IllegalStateException("Plugin work path is not available: $workPath")
    private val indexHelper = JsonlFileHelper<IndexItem> (
        workPath,
        INDEX_FILE_NAME,
        IndexItem::class
    )

    private val init = atomic(false)
    override val type: SourceType = SourceType.JS

    private val _flow: MutableStateFlow<DataState<List<SourceInfo>>> = MutableStateFlow(DataState.none())
    override val flow: Flow<DataState<List<SourceInfo>>>
        get() {
            init()
            return _flow
        }

    @Serializable
    data class IndexItem(
        val key: String,
        val lastModified: Long,
    )

    // lazy load
    fun init() {
        if (init.compareAndSet(expect = false, update = true)) {
            if (workFile == null) {
                logger.error("Plugin work file is not available: $workPath")
                _flow.update {
                    DataState.error("Plugin work file is not available: $workPath")
                }
                return
            }
            _flow.update {
                DataState.loading()
            }
            scope.launch {
                combine(
                    indexHelper.flow(),
                    sourceConfigController.sourceConfigFlow.filterIsInstance<DataState.Ok<Map<String, SourceConfig>>>()
                ) {indexList, configMap ->
                    val res = indexList.map {
                        async() {
                            val con = configMap.data[it.key]
                            val fileName = "${it.key}.${FILE_SUFFIX}"
                            val file = workFileOpt.child(fileName)
                            if (file == null || !file.exists()) {
                                return@async null
                            }
                            val source = file.openSource()
                            val manifestMap = source.buffer().use {
                                PluginFileHelper.getManifestFromNormal(it)
                            }

                            val label = manifestMap[PluginConst.MANIFEST_LABEL_KEY] ?: return@async null
                            val key = manifestMap[PluginConst.MANIFEST_KEY_KEY] ?: return@async null
                            if (key != it.key) {
                                logger.error("Plugin key mismatch: ${it.key} != $key")
                                return@async null
                            }
                            val manifest = SourceManifest(
                                key = manifestMap[PluginConst.MANIFEST_KEY_KEY] ?: return@async null,
                                label = label,
                                version = manifestMap[PluginConst.MANIFEST_VERSION_KEY]?.toIntOrNull() ?: 0,
                                author = manifestMap[PluginConst.MANIFEST_AUTHOR_KEY] ?: "",
                                description = manifestMap[PluginConst.MANIFEST_DESCRIPTION_KEY] ?: "",
                                icon = manifestMap[PluginConst.MANIFEST_ICON_KEY],
                                website = manifestMap[PluginConst.MANIFEST_WEBSITE_KEY],
                                map = manifestMap,
                                type = SourceType.JS,
                                lastModified = it.lastModified,
                                param = file.getUFD(),
                            )
                            if (con?.enable == false) {
                                SourceInfo.Unable(
                                    manifest = manifest,
                                    sourceConfig = con
                                )
                            } else {
                                try {
                                    val loader = loaderFactory.create(manifest)
                                    val componentBundle = loader.load()
                                    SourceInfo.Loaded(
                                        manifest = manifest,
                                        sourceConfig = con ?: SourceConfig(key, enable = true, order = 0),
                                        componentBundle = componentBundle
                                    )
                                } catch (e: Exception) {
                                    logger.error("Plugin load error: ${it.key}", e)
                                    SourceInfo.Error(
                                        manifest = manifest,
                                        sourceConfig = con ?: SourceConfig(key, enable = true, order = 0),
                                        msg = e.message ?: "Unknown error",
                                        exception = e
                                    )
                                }
                            }
                        }

                    }.mapNotNull {
                        it.await()
                    }
                    _flow.update {
                        DataState.ok(res)
                    }
                }.collect()
            }
        }
    }

}
