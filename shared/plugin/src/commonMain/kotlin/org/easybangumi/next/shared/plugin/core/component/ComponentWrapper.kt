package org.easybangumi.next.shared.plugin.core.component

import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.source.Source

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
open class ComponentWrapper: Component {

    var innerSource: Source? = null
    var innerBundle: ComponentBundle? = null

    override val source: Source
        get() = innerSource!!

    protected inline fun <reified T: Any> inject() = lazy {
        innerBundle?.get(T::class) ?: error("Component not found")
    }

}