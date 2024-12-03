package com.heyanle.easy_bangumi_cm.base.preference

import com.heyanle.easy_bangumi_cm.utils.jvm.HeKV
import java.net.URLDecoder
import java.net.URLEncoder

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
                it.map { URLEncoder.encode(it, "utf-8") }.joinToString(",")
            },
            deserializer = {
                it.split(",").map { URLDecoder.decode(it, "utf-8") }.toSet()?:defaultValue
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

    override fun keySet(): Set<String> {
        return heKV.keys()
    }
}