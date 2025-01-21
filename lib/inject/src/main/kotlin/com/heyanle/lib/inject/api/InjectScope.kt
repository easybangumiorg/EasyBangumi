package com.heyanle.lib.inject.api

/**
 * Created by HeYanLe on 2023/7/29 19:35.
 * https://github.com/heyanLE
 */
abstract class InjectScope: InjectRegistry, InjectFactory {
    inline fun <reified T : Any> injectLazy(): Lazy<T> {
        return lazy { get(fullType<T>()) }
    }

    inline fun <reified T : Any> injectValue(): T {
        return get(fullType<T>())
    }

    inline fun <reified T : Any> injectCurrentOrNull(): T? {
        return getCurrentInstanceOrNull(fullType<T>().type)
    }

    inline fun <reified T : Any> injectLazy(key: Any): Lazy<T> {
        return lazy { get(fullType<T>(), key) }
    }

    inline fun <reified T : Any> injectValue(key: Any): T {
        return get(fullType<T>(), key)
    }

    inline fun <reified R : Any> addScopedSingletonFactory(noinline scopedFactoryCalledOnce: InjectScope.() -> R) {
        addSingletonFactory(fullType<R>()) { this.scopedFactoryCalledOnce() }
    }

    inline fun <reified R : Any> addScopedFactory(noinline scopedFactoryCalledEveryTime: InjectScope.() -> R) {
        addFactory(fullType<R>()) { this.scopedFactoryCalledEveryTime() }
    }

    inline fun <reified R : Any, K : Any> addScopedPerKeyFactory(noinline scopedFactoryCalledPerKey: InjectScope.(key: K) -> R) {
        addPerKeyFactory(fullType<R>()) { key: K -> this.scopedFactoryCalledPerKey(key) }
    }

    inline fun <reified R : Any, K : Any> addScopedPerThreadPerKeyFactory(noinline scopedFactoryCalledPerKeyPerThread: InjectScope.(key: K) -> R) {
        addPerThreadPerKeyFactory(fullType<R>()) { key: K -> this.scopedFactoryCalledPerKeyPerThread(key) }
    }

    inline fun <reified R : Any> addScopedPerThreadFactory(noinline scopedFactoryCalledPerThread: InjectScope.() -> R) {
        addPerThreadFactory(fullType<R>()) { this.scopedFactoryCalledPerThread() }
    }

}