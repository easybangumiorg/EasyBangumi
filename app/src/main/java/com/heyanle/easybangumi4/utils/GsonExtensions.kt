package com.heyanle.easybangumi4.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/7/29 21:40.
 * https://github.com/heyanLE
 */

inline fun <reified T> String.jsonTo(): T {
    val gson by Injekt.injectLazy<Gson>()
    return gson.fromJson<T>(this, object: TypeToken<T>() {}.type)
}

fun Any.toJson(): String {
    val gson by Injekt.injectLazy<Gson>()
    return gson.toJson(this)
}