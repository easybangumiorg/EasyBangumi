package com.heyanle.easy_bangumi_cm.base.utils.preference

/**
 * Created by HeYanLe on 2023/7/29 16:48.
 * https://github.com/heyanLE
 */
interface PreferenceStore {

    fun getString(key: String, default: String = ""): Preference<String>

    fun getInt(key: String, default: Int = 0): Preference<Int>

    fun getLong(key: String, default: Long = 0L): Preference<Long>

    fun getFloat(key: String, default:Float = 0f): Preference<Float>

    fun getBoolean(key: String, default: Boolean = false): Preference<Boolean>

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Preference<Set<String>>


    fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T>

    fun keySet(): Set<String>

}

inline fun <reified T : Enum<T>> PreferenceStore.getEnum(
    key: String,
    defaultValue: T,
): Preference<T> {
    return getObject(
        key = key,
        defaultValue = defaultValue,
        serializer = { it.name },
        deserializer = {
            try {
                enumValueOf(it)
            } catch (e: IllegalArgumentException) {
                defaultValue
            }
        },
    )
}