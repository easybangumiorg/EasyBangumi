package org.easybangumi.shared.plugin.bangumi.plugin

import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.ktor.ktorModule
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.inner.InnerSource
import org.easybangumi.shared.plugin.bangumi.business.BangumiApi
import org.easybangumi.shared.plugin.bangumi.business.BangumiBusiness
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

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
        const val SOURCE_ID = "inner_bangumi"
    }

    override val id: String = SOURCE_ID
    override val label: ResourceOr = "Bangumi 番组计划"
    override val icon: ResourceOr? = "https://chii.in/img/favicon.ico"
    override val version: Int = 1
    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::BangumiDiscoverComponent,
    )

    override val module: Module? = module {
        single { BangumiBusiness(get()) }
            .binds(arrayOf(BangumiBusiness::class))
        single { get<BangumiBusiness>().api }
            .binds(arrayOf(BangumiApi::class))

        // source 内部为独立 koin，如果依赖外部 module 需要手动 includes
        includes(ktorModule)
    }
}