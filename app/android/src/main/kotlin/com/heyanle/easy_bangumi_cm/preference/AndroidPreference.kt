package com.heyanle.easy_bangumi_cm.preference

import android.content.SharedPreferences
import androidx.core.content.edit
import com.heyanle.easy_bangumi_cm.base.utils.preference.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Created by HeYanLe on 2023/7/29 16:55.
 * https://github.com/heyanLE
 */
sealed class AndroidPreference<T>(
    private val preferences: SharedPreferences,
    private val keyFlow: Flow<String?>,
    private val key: String,
    private val defaultValue: T,
): Preference<T> {

    abstract fun read(preferences: SharedPreferences, key: String, defaultValue: T): T

    abstract fun write(key: String, value: T): SharedPreferences.Editor.() -> Unit

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return read(preferences, key, defaultValue)
    }

    override fun set(value: T) {
        preferences.edit(action = write(key, value))
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun isSet(): Boolean {
        return preferences.contains(key)
    }

    override fun delete() {
        preferences.edit {
            remove(key)
        }
    }

    override fun flow(): Flow<T> {
        return keyFlow
            .filter { it == key || it == null }
            .onStart { emit("ignition") }
            .map {
                get()
            }.conflate().distinctUntilChanged()
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return flow().stateIn(scope, SharingStarted.Eagerly, get())
    }

    class StringPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: String,
    ) : AndroidPreference<String>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: String): String {
            return preferences.getString(key, defaultValue) ?: defaultValue
        }

        override fun write(key: String, value: String): SharedPreferences.Editor.() -> Unit = {
            putString(key, value)
        }
    }

    class LongPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Long,
    ) : AndroidPreference<Long>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Long): Long {
            return preferences.getLong(key, defaultValue)
        }

        override fun write(key: String, value: Long): SharedPreferences.Editor.() -> Unit = {
            putLong(key, value)
        }
    }

    class IntPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Int,
    ) : AndroidPreference<Int>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Int): Int {
            return preferences.getInt(key, defaultValue)
        }

        override fun write(key: String, value: Int): SharedPreferences.Editor.() -> Unit = {
            putInt(key, value)
        }
    }

    class FloatPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Float,
    ) : AndroidPreference<Float>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Float): Float {
            return preferences.getFloat(key, defaultValue)
        }

        override fun write(key: String, value: Float): SharedPreferences.Editor.() -> Unit = {
            putFloat(key, value)
        }
    }

    class BooleanPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Boolean,
    ) : AndroidPreference<Boolean>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Boolean): Boolean {
            return preferences.getBoolean(key, defaultValue)
        }

        override fun write(key: String, value: Boolean): SharedPreferences.Editor.() -> Unit = {
            putBoolean(key, value)
        }
    }

    class StringSetPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Set<String>,
    ) : AndroidPreference<Set<String>>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Set<String>): Set<String> {
            return preferences.getStringSet(key, defaultValue) ?: defaultValue
        }

        override fun write(key: String, value: Set<String>): SharedPreferences.Editor.() -> Unit = {
            putStringSet(key, value)
        }
    }

    class Object<T>(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: T,
        val serializer: (T) -> String,
        val deserializer: (String) -> T,
    ) : AndroidPreference<T>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: T): T {
            return try {
                preferences.getString(key, null)?.let(deserializer) ?: defaultValue
            } catch (e: Exception) {
                defaultValue
            }
        }

        override fun write(key: String, value: T): SharedPreferences.Editor.() -> Unit = {
            putString(key, serializer(value))
        }
    }



}