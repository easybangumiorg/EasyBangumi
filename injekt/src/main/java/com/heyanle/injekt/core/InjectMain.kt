package com.heyanle.injekt.core

import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.InjektScopedMain
import com.heyanle.injekt.api.fullType
import com.heyanle.injekt.api.get

/**
 * 顶层默认 scope
 * Created by HeYanLe on 2023/7/29 20:00.
 * https://github.com/heyanLE
 */
val Injekt: InjektScope = DefaultInjektScope()

// 使用 object XX : InjektMain 即可在开始时注入
abstract class InjektMain : InjektScopedMain(Injekt)

inline fun <reified T : Any> injectLazy(): Lazy<T> {
    return lazy { Injekt.get(fullType<T>()) }
}

inline fun <reified T : Any> injectValue(): Lazy<T> {
    return lazyOf(Injekt.get(fullType<T>()))
}

inline fun <reified T : Any> injectLazy(key: Any): Lazy<T> {
    return lazy { Injekt.get(fullType<T>(), key) }
}

inline fun <reified T : Any> injectValue(key: Any): Lazy<T> {
    return lazyOf(Injekt.get(fullType<T>(), key))
}