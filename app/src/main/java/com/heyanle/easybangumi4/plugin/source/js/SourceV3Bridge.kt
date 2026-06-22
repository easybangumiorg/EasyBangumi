package com.heyanle.easybangumi4.plugin.source.js

import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.plugin.api.entity.CartoonImpl
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Undefined

object SourceV3Bridge {

    @JvmStatic
    fun makeCartoonCover(sourceKey: String, raw: Any?): CartoonCover {
        return CartoonCoverImpl(
            id = string(raw, "id"),
            source = sourceKey,
            url = string(raw, "url"),
            title = string(raw, "title"),
            intro = nullableString(raw, "intro"),
            coverUrl = nullableString(raw, "cover"),
        )
    }

    @JvmStatic
    fun makeCartoon(sourceKey: String, raw: Any?): Cartoon {
        return CartoonImpl(
            id = string(raw, "id"),
            source = sourceKey,
            url = string(raw, "url"),
            title = string(raw, "title"),
            genre = nullableString(raw, "genre") ?: genreList(raw),
            coverUrl = nullableString(raw, "cover"),
            intro = nullableString(raw, "intro"),
            description = nullableString(raw, "description"),
            updateStrategy = int(raw, "updateStrategy", Cartoon.UPDATE_STRATEGY_ALWAYS),
            isUpdate = boolean(raw, "isUpdate", false),
            status = int(raw, "status", Cartoon.STATUS_UNKNOWN),
        )
    }

    private fun string(raw: Any?, key: String): String {
        return nullableString(raw, key).orEmpty()
    }

    private fun nullableString(raw: Any?, key: String): String? {
        val value = value(raw, key) ?: return null
        return value.toString().takeIf { it.isNotBlank() }
    }

    private fun int(raw: Any?, key: String, def: Int): Int {
        val value = value(raw, key) ?: return def
        return when (value) {
            is Number -> value.toInt()
            else -> value.toString().toDoubleOrNull()?.toInt() ?: def
        }
    }

    private fun boolean(raw: Any?, key: String, def: Boolean): Boolean {
        val value = value(raw, key) ?: return def
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            else -> value.toString().equals("true", ignoreCase = true)
        }
    }

    private fun genreList(raw: Any?): String? {
        val value = value(raw, "genreList") ?: return null
        val list = when (value) {
            is NativeArray -> value.ids
                .mapNotNull {
                    when (it) {
                        is Number -> value.get(it.toInt(), value)
                        else -> value.get(it.toString(), value)
                    }
                }
                .mapNotNull { it.cleanString() }
            is Iterable<*> -> value.mapNotNull { it.cleanString() }
            is Array<*> -> value.mapNotNull { it.cleanString() }
            else -> emptyList()
        }
        return list.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    private fun value(raw: Any?, key: String): Any? {
        val value = when (raw) {
            is NativeObject -> raw.get(key, raw)
            is Map<*, *> -> raw[key]
            else -> null
        }
        return value?.takeUnless { it == Undefined.instance }
    }

    private fun Any?.cleanString(): String? {
        return this?.takeUnless { it == Undefined.instance }
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
