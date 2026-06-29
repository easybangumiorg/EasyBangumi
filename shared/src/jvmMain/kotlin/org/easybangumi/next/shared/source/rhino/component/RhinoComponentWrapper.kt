package org.easybangumi.next.shared.source.rhino.component

import org.easybangumi.next.rhino.RhinoScope
import kotlin.reflect.KClass

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
interface RhinoComponentWrapper {

    interface Factory<T : RhinoComponentWrapper> {

        suspend fun create(
            rhinoScope: RhinoScope
        ): T?

    }

    fun getComponentClazz(): Array<KClass<*>>

}