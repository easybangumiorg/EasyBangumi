package com.heyanle.easybangumi.utils

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Created by HeYanLe on 2021/11/19 20:20.
 * https://github.com/heyanLE
 */
inline fun <reified T> fromJson(string: String): T{
    return GsonUtils.gson.fromJson<T>(string,  (object : TypeToken<T>() {}).type)
}
fun <T> toJson(obj: T) : String{
    return GsonUtils.gson.toJson(obj)
}
object GsonUtils {
    val gson : Gson by lazy {
        GsonBuilder()
            .create()
    }
}