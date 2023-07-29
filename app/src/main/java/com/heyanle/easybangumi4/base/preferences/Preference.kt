package com.heyanle.easybangumi4.base.preferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by HeYanLe on 2023/7/29 16:44.
 * https://github.com/heyanLE
 */
interface Preference <T> {

    fun key(): String

    fun get(): T

    fun set(value: T)

    fun defaultValue(): T

    fun isSet(): Boolean

    fun delete()

    fun flow(): Flow<T>

    fun stateIn(scope: CoroutineScope): StateFlow<T>

}

inline fun <reified T, R : T> Preference<T>.getAndSet(crossinline block: (T) -> R) = set(block(get()))