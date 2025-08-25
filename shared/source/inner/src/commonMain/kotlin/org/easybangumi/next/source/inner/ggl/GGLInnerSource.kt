package org.easybangumi.next.source.inner.ggl

import io.ktor.client.HttpClient
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.next.shared.ktor.ktorModule
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.source.InnerSource
import org.koin.core.module.Module
import org.koin.dsl.bind
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
class GGLInnerSource: InnerSource() {

    companion object {
        const val SOURCE_KEY = "ggl"
    }

    override val key: String = SOURCE_KEY
    override val label: ResourceOr = "GiriGiriLove"
    override val icon: ResourceOr = "https://anime.girigirilove.com/upload/site/20231121-1/fdd2694db66628a9deadd86e50aedd43.png"
    override val version: Int = 1

    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::GGLPlayComponent,
        ::GGLPrefComponent,
        ::GGLSearchComponent,
    )

    override val module: Module
        get() = module {
            includes(ktorModule)
            single {
                get<KtorFactory>().create()
            }.bind<HttpClient>()
        }

}