package org.easybangumi.next.lib.serialization

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

expect val jsonSerializer: JsonSerializer

//object JsonSerializer
//
//expect fun <T: Any> JsonSerializer.serialize(data: T): String
//
//expect fun <T: Any> JsonSerializer.deserialize(data: String, clazz: KClass<T>, defaultValue: T?): T?

inline fun <reified T: Any> JsonSerializer.deserialize(data: String, defaultValue: T?): T? {
    return deserialize(data, T::class, defaultValue)
}