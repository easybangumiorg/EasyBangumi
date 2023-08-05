package com.heyanle.easybangumi4.base.preferences.mmkv

import com.heyanle.easybangumi4.base.preferences.Preference
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Created by HeYanLe on 2023/7/30 19:15.
 * https://github.com/heyanLE
 */
class MMKVPreference<T : Any>(
    private val key: String,
    private val defaultValue: T,
) : Preference<T> {

    private var valueOkkv by okkv(key, def = defaultValue)
    private val flow = MutableStateFlow(valueOkkv)

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return valueOkkv
    }

    override fun set(value: T) {
        valueOkkv = value
        flow.update {
            value
        }
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun isSet(): Boolean {
        return true
    }

    override fun delete() {
        set(defaultValue)
    }

    override fun flow(): Flow<T> {
        return flow
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return flow.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

class MMKVObjectPreference<T>(
    private val key: String,
    private val defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
) : Preference<T> {

    private var valueOkkv by okkv(key, def = serializer(defaultValue))
    private val flow = MutableStateFlow(defaultValue)
    override fun key(): String {
        return key
    }

    override fun get(): T {
        return deserializer(valueOkkv)
    }

    override fun set(value: T) {
        valueOkkv = serializer(value)
        flow.update {
            value
        }
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun isSet(): Boolean {
        return true
    }

    override fun delete() {
        set(defaultValue)
    }

    override fun flow(): Flow<T> {
        return flow
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return flow.stateIn(scope, SharingStarted.Eagerly, get())
    }
}