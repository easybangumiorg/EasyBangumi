package com.heyanle.easy_bangumi_cm.shared.utils

import com.heyanle.easy_bangumi_cm.shared.base.Platform
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

/**
 * Created by HeYanLe on 2023/7/29 21:40.
 * https://github.com/heyanLE
 */


@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> moshiAdapter(): JsonAdapter<T> {
    val moshi: Moshi by Inject.injectLazy()
    return moshi.adapter<T>(typeOf<T>().javaType)
}

fun <T> moshiAdapter(type: Type): JsonAdapter<T> {
    val moshi: Moshi by Inject.injectLazy()
    return moshi.adapter(type)
}


inline fun <reified T> String.jsonTo(): T? {
    val adapter = moshiAdapter<T>()
    if (isEmpty()) {
        return null
    }
    return runCatching {
        adapter.fromJson(this)
    }.getOrElse {
        val platform = Inject.get<Platform>()
        if (!platform.isRelease) {
            throw it
        } else {
            null
        }
    }
}


inline fun <reified T> T.toJson(): String {
    val adapter = moshiAdapter<T>()
    return adapter.toJson(this)
}


fun <T: Any> T.toJson(type: Type): String {
    val adapter = moshiAdapter<T>(type)
    return adapter.toJson(this)
}

fun <T: Any> String.jsonTo(type: Type): T? {
    val adapter = moshiAdapter<T>(type)
    if (isEmpty()) {
        return null
    }
    return runCatching {
        adapter.fromJson(this)
    }.getOrElse {
        val platform = Inject.get<Platform>()
        if (!platform.isRelease) {
            throw it
        } else {
            null
        }
    }
}