﻿package org.easybangumi.next.shared.source.core.inner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.source.api.SourceProvider
import org.easybangumi.next.shared.source.api.component.ComponentBundle
import org.easybangumi.next.shared.source.api.source.SourceConfig
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.api.source.SourceType
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource
import org.easybangumi.next.shared.source.core.source.SourceConfigController

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

    // 业务如果确保只需要特定 Inner 源 （例如 Bangumi 首页）可直接在这里读取，如果从 SourceController 读取则需要等待其他源加载完毕
    val bangumiSource by lazy {
        BangumiInnerSource()
    }
    val bangumiComponentBundle: ComponentBundle by lazy {
        InnerComponentBundle(bangumiSource).apply { runBlocking {
            load()
        }}
    }
    override val type: SourceType = SourceType.INNER


    override val flow: Flow<DataState<List<SourceInfo>>> by lazy {
        sourceConfigController.sourceConfigFlow.map {
            it.map {
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


                )
            }
        }
    }




}