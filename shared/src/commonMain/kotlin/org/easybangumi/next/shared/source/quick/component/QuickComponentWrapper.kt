package org.easybangumi.next.shared.source.quick.component

import com.dokar.quickjs.QuickJs
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
interface QuickComponentWrapper {

    interface Factory<T : QuickComponentWrapper> {

        suspend fun create(
            quickJs: QuickJs
        ): T?

    }

    fun getComponentClazz(): Array<KClass<*>>
}