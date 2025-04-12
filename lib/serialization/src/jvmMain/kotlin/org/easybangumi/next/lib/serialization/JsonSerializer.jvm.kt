package org.easybangumi.next.lib.serialization

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.easybangumi.next.lib.serialization.moshi.MoshiArrayListJsonAdapter
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
private class JsonSerializerImpl: JsonSerializer {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(MoshiArrayListJsonAdapter.FACTORY)
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private fun <T : Any> Moshi.adapter(kClass: KClass<T>): JsonAdapter<T> {
        return moshi.adapter(kClass.java)
    }

    override fun <T : Any> serialize(data: T): String {
        val adapter = moshi.adapter<T>(data.javaClass)
        return runCatching {
            adapter?.toJson(data) ?: ""
        }.getOrElse {
            ""
        }
    }

    override fun <T : Any> deserialize(
        data: String,
        clazz: KClass<T>,
        defaultValue: T?
    ): T? {
        val adapter = moshi.adapter(clazz)
        if (data.isEmpty()) {
            return null
        }
        return runCatching {
            adapter.fromJson(data)
        }.getOrElse {
            defaultValue
        }
    }
}



actual val jsonSerializer: JsonSerializer by lazy {
    JsonSerializerImpl()
}