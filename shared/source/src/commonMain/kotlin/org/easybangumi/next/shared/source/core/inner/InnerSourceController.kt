package org.easybangumi.next.shared.source.core.inner

import org.easybangumi.next.shared.source.api.source.SourceConfig
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource
import org.koin.core.component.KoinComponent

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

class InnerSourceController: KoinComponent {


    val bangumiSourceInfo: SourceInfo.Loaded by lazy {
        val bangumiInnerSource = BangumiInnerSource()
        val componentBundle = InnerComponentBundle(bangumiInnerSource)
        componentBundle.load()
        // inner source can't disable
        SourceInfo.Loaded(
            manifest = bangumiInnerSource.manifest,
            sourceConfig = SourceConfig(
                key = bangumiInnerSource.key,
                enable = true,
                order = -1
            ),
            componentBundle = componentBundle
        )
    }


    val innerSourceInfoList: List<SourceInfo.Loaded> by lazy {
        listOf(
            bangumiSourceInfo
        )
    }




}