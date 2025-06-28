package org.easybangumi.next.shared.plugin.api.component

import org.easybangumi.next.shared.plugin.api.source.Source
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
interface ComponentBundle {

    fun getSource(): Source

    suspend fun load()

    fun <T : Any> get(clazz: KClass<T>): T?

    fun <T: Component> getBusiness(clazz: KClass<T>): ComponentBusiness<T>?

    fun release()

}

inline fun <reified T : Component> ComponentBundle.getBusiness(): ComponentBusiness<T>? {
    return getBusiness(T::class)
}

inline fun <reified T : Any> ComponentBundle.get(): T? {
    return get(T::class)
}