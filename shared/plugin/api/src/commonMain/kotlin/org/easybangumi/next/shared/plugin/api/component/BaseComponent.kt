package org.easybangumi.next.shared.plugin.api.component

import org.easybangumi.next.shared.plugin.api.source.Source
import org.koin.core.Koin
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
open class BaseComponent: Component, KoinComponent {

    var innerKoin: Koin? = null
    var innerSource: Source? = null

    override val source: Source
        get() = innerSource!!

    override fun getKoin(): Koin {
        return innerKoin!!
    }

}