package com.heyanle.lib.inject.api

import java.lang.reflect.Type

/**
 * Created by HeYanLe on 2023/7/29 19:32.
 * https://github.com/heyanLE
 */
interface InjectFactory {
    fun <R: Any> getInstance(forType: Type): R

    // 返回当前容器中缓存的实例，如果没有则返回 null
    // 对于 factory 模式，直接返回 null
    fun <R: Any> getCurrentInstanceOrNull(forType: Type): R?

    fun <R: Any, K: Any> getKeyedInstance(forType: Type, key: K): R


}

@Suppress("NOTHING_TO_INLINE")
inline fun <R: Any> InjectFactory.get(forType: TypeReference<R>): R = getInstance(forType.type)

@Suppress("NOTHING_TO_INLINE")
inline fun <R: Any> InjectFactory.get(forType: TypeReference<R>, key: Any): R = getKeyedInstance(forType.type, key)

inline fun <reified R: Any> InjectFactory.get(key: Any): R = getKeyedInstance(fullType<R>().type, key)

inline fun <reified R: Any> InjectFactory.getOrNull(): R? = runCatching {
    get<R>()
}.getOrNull()

inline fun <reified R: Any> InjectFactory.get(): R = getInstance(fullType<R>().type)