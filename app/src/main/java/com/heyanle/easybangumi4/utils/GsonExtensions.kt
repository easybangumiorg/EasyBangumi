package com.heyanle.easybangumi4.utils

import org.koin.mp.KoinPlatform.getKoin
import com.squareup.moshi.Moshi
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * Created by HeYanLe on 2023/7/29 21:40.
 * https://github.com/heyanLE
 */

inline fun <reified T> String.jsonTo(): T? {
    val moshi: Moshi by getKoin().inject()
    val adapter = moshi.adapter<T>(typeOf<T>().javaType)
    return adapter.fromJson(this)
}


inline fun <reified T>  T.toJson(): String {
    val moshi: Moshi by getKoin().inject()
    val adapter = moshi.adapter<T>(typeOf<T>().javaType)
    return adapter.toJson(this)
}