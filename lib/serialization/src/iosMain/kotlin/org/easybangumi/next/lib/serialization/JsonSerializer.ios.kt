package org.easybangumi.next.lib.serialization

import kotlin.reflect.KClass

actual fun <T : Any> JsonSerializer.serialize(data: T): String {
    TODO("Not yet implemented")
}

actual fun <T : Any> JsonSerializer.deserialize(
    data: String,
    clazz: KClass<T>,
    defaultValue: T?
): T? {
    TODO("Not yet implemented")
}