package com.heyanle.injekt.api

import kotlin.reflect.KClass

/**
 * Created by HeYanLe on 2023/7/29 19:33.
 * https://github.com/heyanLE
 */
interface InjektRegistry {
    fun <T : Any> addSingleton(forType: TypeReference<T>, singleInstance: T) {
        addSingletonFactory (forType){ singleInstance }
    }
    fun <R: Any> addSingletonFactory(forType: TypeReference<R>, factoryCalledOnce: () -> R)
    fun <R: Any> addFactory(forType: TypeReference<R>, factoryCalledEveryTime: () -> R)
    fun <R: Any> addPerThreadFactory(forType: TypeReference<R>, factoryCalledOncePerThread: () -> R)
    fun <R: Any, K: Any> addPerKeyFactory(forType: TypeReference<R>, factoryCalledPerKey: (K) -> R)
    fun <R: Any, K: Any> addPerThreadPerKeyFactory(forType: TypeReference<R>, factoryCalledPerKeyPerThread: (K) -> R)
    fun <T: Any> hasFactory(forType: TypeReference<T>): Boolean

    fun <O: Any, T: O> addAlias(existingRegisteredType: TypeReference<T>, otherAncestorOrInterface: TypeReference<O>)
}

inline fun <reified T: Any> InjektRegistry.hasFactory(): Boolean {
    return hasFactory(fullType<T>())
}

inline fun <reified T : Any> InjektRegistry.addSingleton(singleInstance: T) {
    addSingleton(fullType<T>(), singleInstance)
}

inline fun <reified R: Any> InjektRegistry.addSingletonFactory(noinline factoryCalledOnce: () -> R) {
    addSingletonFactory(fullType<R>(), factoryCalledOnce)
}

inline fun <reified R: Any> InjektRegistry.addFactory(noinline factoryCalledEveryTime: () -> R) {
    addFactory(fullType<R>(), factoryCalledEveryTime)
}

inline fun <reified R: Any> InjektRegistry.addPerThreadFactory(noinline factoryCalledOncePerThread: () -> R) {
    addPerThreadFactory(fullType<R>(), factoryCalledOncePerThread)
}

inline fun <reified R: Any, K: Any> InjektRegistry.addPerKeyFactory(noinline factoryCalledPerKey: (K) -> R) {
    addPerKeyFactory(fullType<R>(), factoryCalledPerKey)
}

inline fun <reified R: Any, K: Any> InjektRegistry.addPerThreadPerKeyFactory(noinline factoryCalledPerKeyPerThread: (K) -> R) {
    addPerThreadPerKeyFactory(fullType<R>(), factoryCalledPerKeyPerThread)
}

inline fun <reified EXISTINGREGISTERED: ANCESTORTYPE, reified ANCESTORTYPE: Any> InjektRegistry.addAlias() = addAlias(fullType<EXISTINGREGISTERED>(), fullType<ANCESTORTYPE>())
