﻿package org.easybangumi.next.shared.source.bangumi.component

import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.source.InnerSource
import org.easybangumi.next.shared.source.bangumi.bangumiModule
import org.koin.core.module.Module

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

class BangumiInnerSource: InnerSource() {

    companion object {
        const val SOURCE_KEY = "bangumi"
    }

    override val key: String = SOURCE_KEY
    override val label: ResourceOr = "番组计划"
    override val icon: ResourceOr? = null
    override val version: Int = 1

    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::BangumiDiscoverComponent
    )

    override val module: Module?
        get() = bangumiModule
}