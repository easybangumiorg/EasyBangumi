package org.easybangumi.next.lib.store.file_helper.json

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okio.BufferedSink
import okio.BufferedSource
import org.easybangumi.next.lib.serialization.jsonSerializer
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

class JsonlFileHelper<T: Any> (
    folder: UFD,
    name: String,
    private val clazz: KClass<T>,
): AbsFileHelper<List<T>>(folder, name, emptyList()) {

    override fun suffix(): String {
        return "json"
    }

    override fun serializer(data: List<T>, sink: BufferedSink) {
        data.forEach {
            sink.writeUtf8(jsonSerializer.serialize(it))
            sink.writeUtf8("\n")
        }
    }

    override fun deserializer(source: BufferedSource): List<T>? {
        val res = arrayListOf<T>()
        while (true) {
            val line = source.readUtf8Line() ?: break
            val data = jsonSerializer.deserialize(line, clazz, null) ?: break
            res.add(data)
        }
        return res
    }
}