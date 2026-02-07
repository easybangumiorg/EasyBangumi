package org.easybangumi.next.shared.source.core.inner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.source.api.SourceProvider
import org.easybangumi.next.shared.source.api.component.ComponentBundle
import org.easybangumi.next.shared.source.api.source.SourceConfig
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.api.source.SourceType
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource
import org.easybangumi.next.shared.source.core.source.SourceConfigController
import org.easybangumi.next.source.inner.age.AgeInnerSource
import org.easybangumi.next.source.inner.debug.DebugInnerSource
import org.easybangumi.next.source.inner.ggl.GGLInnerSource
import org.easybangumi.next.source.inner.xifan.XifanInnerSource

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

class InnerSourceProvider(
    private val sourceConfigController: SourceConfigController,
): SourceProvider {

    // 业务如果确保只需要特定 Inner 源 （例如 Bangumi 首页）可直接在这里读取，如果从 SourceController 读取则需要等待所有源加载完毕
    val bangumiSource by lazy {
        BangumiInnerSource()
    }
    val bangumiComponentBundle: ComponentBundle by lazy {
        InnerComponentBundle(bangumiSource).apply { runBlocking {
            load()
        }}
    }


    val gglSource by lazy {
        GGLInnerSource()
    }
    val gglComponentBundle: ComponentBundle by lazy {
        InnerComponentBundle(gglSource).apply { runBlocking {
            load()
        }}
    }

    val xifanSource by lazy {
        XifanInnerSource()
    }
    val xifanComponentBundle: ComponentBundle by lazy {
        InnerComponentBundle(xifanSource).apply { runBlocking {
            load()
        }}
    }

    val ageSource by lazy {
        AgeInnerSource()
    }
    val ageComponentBundle by lazy {
        InnerComponentBundle(ageSource).apply { runBlocking {
            load()
        }}
    }

    val debugSource by lazy {
        DebugInnerSource()
    }

    val debugComponentBundle: ComponentBundle by lazy {
        InnerComponentBundle(debugSource).apply { runBlocking {
            load()
        }}
    }




    override val type: SourceType = SourceType.INNER



    override val flow: Flow<DataState<List<SourceInfo>>> by lazy {
        sourceConfigController.sourceConfigFlow.map {
            it.map { map ->
                listOf(
                    // bangumi 无法关闭和排序
                    SourceInfo.Loaded(
                        manifest = bangumiSource.manifest,
                        sourceConfig = SourceConfig(
                            key = bangumiSource.key,
                            enable = true,
                            order = -1
                        ),
                        componentBundle = bangumiComponentBundle
                    ),
                    SourceInfo.Loaded(
                        manifest = gglSource.manifest,
                        sourceConfig =  map.getOrElse(gglSource.key) {
                            SourceConfig(
                                key = gglSource.key,
                                enable = true,
                                order = 0,
                            )
                        },
                        componentBundle = gglComponentBundle
                    ),
                    SourceInfo.Loaded(
                        manifest = xifanSource.manifest,
                        sourceConfig =  map.getOrElse(xifanSource.key) {
                            SourceConfig(
                                key = xifanSource.key,
                                enable = true,
                                order = 0,
                            )
                        },
                        componentBundle = xifanComponentBundle
                    ),
                    SourceInfo.Loaded(
                        manifest = ageSource.manifest,
                        sourceConfig =  map.getOrElse(ageSource.key) {
                            SourceConfig(
                                key = ageSource.key,
                                enable = true,
                                order = 0,
                            )
                        },
                        componentBundle = ageComponentBundle
                    )
                    ,


                ).also {
                    if (platformInformation.isDebug) {
                        it +  SourceInfo.Loaded(
                            manifest = debugSource.manifest,
                            sourceConfig =  map.getOrElse(debugSource.key) {
                                SourceConfig(
                                    key = debugSource.key,
                                    enable = false,
                                    order = 1,
                                )
                            },
                            componentBundle = debugComponentBundle
                        )
                    }
                }
            }
        }
    }




}