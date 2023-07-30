package com.heyanle.injekt.api

/**
 * Created by HeYanLe on 2023/7/29 19:35.
 * https://github.com/heyanLE
 */
abstract class InjektScope: InjektRegistry, InjektFactory {
    inline fun <reified T : Any> injectLazy(): Lazy<T> {
        return lazy { get(fullType<T>()) }
    }

    inline fun <reified T : Any> injectValue(): Lazy<T> {
        return lazyOf(get(fullType<T>()))
    }

    inline fun <reified T : Any> injectLazy(key: Any): Lazy<T> {
        return lazy { get(fullType<T>(), key) }
    }

    inline fun <reified T : Any> injectValue(key: Any): Lazy<T> {
        return lazyOf(get(fullType<T>(), key))
    }

    inline fun <reified R : Any> addScopedSingletonFactory(noinline scopedFactoryCalledOnce: InjektScope.() -> R) {
        addSingletonFactory(fullType<R>()) { this.scopedFactoryCalledOnce() }
    }

    inline fun <reified R : Any> addScopedFactory(noinline scopedFactoryCalledEveryTime: InjektScope.() -> R) {
        addFactory(fullType<R>()) { this.scopedFactoryCalledEveryTime() }
    }

    inline fun <reified R : Any, K : Any> addScopedPerKeyFactory(noinline scopedFactoryCalledPerKey: InjektScope.(key: K) -> R) {
        addPerKeyFactory(fullType<R>()) { key: K -> this.scopedFactoryCalledPerKey(key) }
    }

    inline fun <reified R : Any, K : Any> addScopedPerThreadPerKeyFactory(noinline scopedFactoryCalledPerKeyPerThread: InjektScope.(key: K) -> R) {
        addPerThreadPerKeyFactory(fullType<R>()) { key: K -> this.scopedFactoryCalledPerKeyPerThread(key) }
    }

    inline fun <reified R : Any> addScopedPerThreadFactory(noinline scopedFactoryCalledPerThread: InjektScope.() -> R) {
        addPerThreadFactory(fullType<R>()) { this.scopedFactoryCalledPerThread() }
    }

}