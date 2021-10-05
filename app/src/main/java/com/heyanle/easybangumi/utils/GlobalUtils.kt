package com.heyanle.easybangumi.utils

import java.util.*
import kotlin.collections.HashMap

/**
 * Created by HeYanLe on 2021/9/21 13:49.
 * https://github.com/heyanLE
 */
object GlobalUtils {

    private val map = Collections.synchronizedMap(hashMapOf<String, Any>())

    fun put(key: String, obj: Any){
        map[key] = obj
    }

    fun <T> get(key: String?) : T?{
        if(key == null){
            return null
        }
        return map[key] as T?
    }

}