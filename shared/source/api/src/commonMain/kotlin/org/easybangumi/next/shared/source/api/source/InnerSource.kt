package org.easybangumi.next.shared.source.api.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.source.api.component.Component

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


abstract class InnerSource: Source {


    abstract override val key: String
    abstract val label: ResourceOr
    abstract val icon: ResourceOr?
    abstract val version: Int

    open val description: String? = null
    open val website: String? = null
    open val author: String = "Heyanle"

    override val manifest: SourceManifest by lazy {
        SourceManifest(
            key = key,
            label = label,
            icon = icon,
            version = version,
            author = author,
            description = description,
            website = website,
            map = emptyMap(),
            lastModified = Clock.System.now().toEpochMilliseconds(),
            type = SourceType.INNER,
        )
    }

    abstract val componentConstructor: Array<()-> Component>

    override val workPath: UFD
        get() = throw IllegalStateException("workPath not support in inner source, please use SourceWrapper")

    override val scope: CoroutineScope
        get() = throw IllegalStateException("scope not support in inner source, please use SourceWrapper")



}