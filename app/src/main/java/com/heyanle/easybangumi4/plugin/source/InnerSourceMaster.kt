package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.plugin.source.bundle.SimpleComponentBundle

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
object InnerSourceMaster {

    private val localSource: LocalSource by lazy {
        LocalSource
    }
    val localComponentBundle: SimpleComponentBundle by lazy {
        SimpleComponentBundle(localSource).apply {
            initSync()
        }
    }
    val localSourceInfo: SourceInfo by lazy {
        SourceInfo.Loaded(
            source = localSource,
            componentBundle = localComponentBundle
        )
    }
    val localSourceConfig: SourceConfig by lazy {
        SourceConfig(
            key = localSource.key,
            enable = true,
            order = Int.MAX_VALUE
        )
    }
    val localConfigSource: ConfigSource by lazy {
        ConfigSource(
            sourceInfo = localSourceInfo,
            config = localSourceConfig
        )
    }

}