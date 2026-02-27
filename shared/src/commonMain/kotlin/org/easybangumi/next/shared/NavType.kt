package org.easybangumi.next.shared

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.shared.compose.web.WebPageParam
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
val NavTypeMap = mapOf(
    typeOf<CartoonIndex>() to JsonStringNavType(CartoonIndex::class),
    typeOf<CartoonCover>() to JsonStringNavType(CartoonCover::class),
    typeOf<WebPageParam>() to JsonStringNavType(WebPageParam::class),
    typeOf<CartoonIndex?>() to JsonStringNullableNavType(CartoonIndex::class),
    typeOf<CartoonCover?>() to JsonStringNullableNavType(CartoonCover::class),
    typeOf<WebPageParam?>() to JsonStringNullableNavType(WebPageParam::class),
)

class JsonStringNullableNavType<T : Any>(
    private val clazz: KClass<T>,
): NavType<T?>(true) {
    override fun get(bundle: SavedState, key: String): T? {
        val value = bundle.read {
            getString(key)
        }
        return parseValue(value)
    }

    override fun put(bundle: SavedState, key: String, value: T?) {
        bundle.write {
            putString(key, serializeAsValue(value))
        }
    }

    override fun serializeAsValue(value: T?): String {
        value ?: return ""
        return jsonSerializer.serialize(value)
    }

    override fun parseValue(value: String): T? {
        if (value.isEmpty()) return null
        return jsonSerializer.deserialize(value, clazz, null)
            ?: throw IllegalArgumentException("Cannot parse value: $value")
    }


}

class JsonStringNavType<T : Any>(
    private val clazz: KClass<T>,
): NavType<T>(false) {
    override fun get(bundle: SavedState, key: String): T? {
        val value = bundle.read {
            getString(key)
        }
        return parseValue(value)
    }

    override fun put(bundle: SavedState, key: String, value: T) {
        bundle.write {
            putString(key, serializeAsValue(value))
        }
    }

    override fun serializeAsValue(value: T): String {
        return jsonSerializer.serialize(value)
    }

    override fun parseValue(value: String): T {
        return jsonSerializer.deserialize(value, clazz, null)
            ?: throw IllegalArgumentException("Cannot parse value: $value")
    }


}