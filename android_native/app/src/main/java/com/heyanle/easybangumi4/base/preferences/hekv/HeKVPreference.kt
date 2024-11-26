package com.heyanle.easybangumi4.base.preferences.hekv

import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.base.preferences.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Created by HeYanLe on 2023/8/5 19:20.
 * https://github.com/heyanLE
 */
class HeKVPreference<T>(
    private val heKV: HeKV,
    private val key: String,
    private val def: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
): Preference<T> {

    private val realDef = serializer(def)

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return deserializer(heKV.get(key, realDef))
    }

    override fun set(value: T) {
        heKV.put(key, serializer(value))
    }

    override fun defaultValue(): T {
        return def
    }

    override fun isSet(): Boolean {
        return heKV.get(key, "") != ""
    }

    override fun delete() {
        heKV.put(key, "")
    }

    override fun flow(): Flow<T> {
        return heKV.flow(key, realDef).map {
            deserializer(it)
        }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return flow().stateIn(scope, SharingStarted.Eagerly, get())
    }
}