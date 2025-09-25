package org.easybangumi.next.source.inner.debug

import io.ktor.client.HttpClient
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.next.shared.ktor.ktorModule
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.source.InnerSource
import org.easybangumi.next.source.inner.ggl.GGLPlayComponent
import org.easybangumi.next.source.inner.ggl.GGLPrefComponent
import org.easybangumi.next.source.inner.ggl.GGLSearchComponent
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
class DebugInnerSource : InnerSource() {

    companion object {
        const val SOURCE_KEY = "debug"
    }

    override val key: String = SOURCE_KEY
    override val label: ResourceOr = "Debug"
    override val icon: ResourceOr = Res.images.logo
    override val version: Int = 1

    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::DebugComponent,
    )

    override val module: Module
        get() = module {
            includes(ktorModule)
            single {
                get<KtorFactory>().create()
            }.bind<HttpClient>()
        }

}