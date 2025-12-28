package org.easybangumi.next.lib.store.file_helper.json

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okio.BufferedSink
import okio.BufferedSource
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.store.file_helper.AbsFileHelper
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.coroutineProvider
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
class JsonFileHelper<T: Any>(
    folder: UFD,
    name: String,
    private val clazz: KClass<T>,
    def: T,
    isLogger: Boolean = false,
): AbsFileHelper<T>(folder, name, def, isLogger) {

    override fun suffix(): String {
        return "json"
    }

    override fun serializer(data: T, sink: BufferedSink) {
        val json = jsonSerializer.serialize(data)
        sink.writeUtf8(json)
    }

    override fun deserializer(source: BufferedSource): T? {
        val json = source.readByteString().utf8()
        return jsonSerializer.deserialize(json, clazz, null)
    }
}