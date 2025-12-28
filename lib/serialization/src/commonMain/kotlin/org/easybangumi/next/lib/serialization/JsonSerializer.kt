package org.easybangumi.next.lib.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
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
val jsonSerializer: JsonSerializer = JsonSerializer
object JsonSerializer

val json: Json by lazy {
    Json
}

inline fun <reified T: Any> JsonSerializer.deserialize(data: String, defaultValue: T?): T? {
    return runCatching {
        json.decodeFromString<T>(data)
    }.getOrElse {
        defaultValue
    }
}

// not support top-level array
@OptIn(InternalSerializationApi::class)
fun <T: Any> JsonSerializer.deserialize(data: String, clazz: KClass<T>, defaultValue: T?): T? {
    return runCatching {
        json.decodeFromString(clazz.serializer(), data)
    }.getOrElse {
        it.printStackTrace()
        defaultValue
    }
}


@OptIn(InternalSerializationApi::class)
fun <T : Any> JsonSerializer.serialize(data: T): String {
    return json.encodeToString<T>(data::class.serializer() as SerializationStrategy<T>, data)
}
