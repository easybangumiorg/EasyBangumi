package com.heyanle.easybangumi4.base.preferences.hekv

import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.base.preferences.Preference
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson

/**
 * Created by HeYanLe on 2023/8/5 19:17.
 * https://github.com/heyanLE
 */
class HeKVPreferenceStore(
    private val heKV: HeKV
): PreferenceStore {

    override fun getString(key: String, default: String): Preference<String> {
        return HeKVPreference<String>(
            heKV,
            key,
            default,
            {it},
            {it}
        )
    }

    override fun getInt(key: String, default: Int): Preference<Int> {
        return HeKVPreference<Int>(
            heKV,
            key,
            default,
            {it.toString()},
            {it.toIntOrNull()?:default}
        )
    }

    override fun getLong(key: String, default: Long): Preference<Long> {
        return HeKVPreference<Long>(
            heKV,
            key,
            default,
            {it.toString()},
            {it.toLongOrNull()?:default}
        )
    }

    override fun getFloat(key: String, default: Float): Preference<Float> {
        return HeKVPreference<Float>(
            heKV,
            key,
            default,
            {it.toString()},
            {it.toFloatOrNull()?:default}
        )
    }

    override fun getBoolean(key: String, default: Boolean): Preference<Boolean> {
        return HeKVPreference<Boolean>(
            heKV,
            key,
            default,
            {it.toString()},
            {it.toBooleanStrictOrNull()?:default}
        )
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return HeKVPreference<Set<String>>(
            heKV,
            key,
            defaultValue,
            serializer = {
                it.toList().toJson()
            },
            deserializer = {
                val d: List<String> = it.jsonTo()
                d.toSet()
            }
        )
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> {
        return HeKVPreference<T>(
            heKV,
            key,
            defaultValue,
            serializer = serializer,
            deserializer = deserializer
        )
    }
}