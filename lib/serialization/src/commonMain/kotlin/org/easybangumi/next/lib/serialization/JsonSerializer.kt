package org.easybangumi.next.lib.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

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

interface JsonSerializer {
    fun <T: Any> serialize(data: T): String
    fun <T: Any> deserialize(data: String, clazz: KClass<T>, defaultValue: T?): T?


}

class JsonSerializerImpl: JsonSerializer {
    private val json: Json by lazy {
        Json
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> serialize(data: T): String {
        return json.encodeToString<T>(data::class.serializer() as SerializationStrategy<T>, data)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> deserialize(
        data: String,
        clazz: KClass<T>,
        defaultValue: T?
    ): T? {
        return runCatching {
            Json.decodeFromString(clazz.serializer(), data)
        }.getOrElse {
            it.printStackTrace()
            defaultValue
        }
    }
}

val jsonSerializer: JsonSerializer = JsonSerializerImpl()

inline fun <reified T: Any> JsonSerializer.deserialize(data: String, defaultValue: T?): T? {
    return deserialize(data, T::class, defaultValue)
}