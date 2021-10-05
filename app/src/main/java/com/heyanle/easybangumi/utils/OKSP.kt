package com.heyanle.easybangumi.utils

import android.content.Context
import android.content.SharedPreferences
import com.heyanle.easybangumi.EasyApplication
import kotlin.reflect.KProperty

/**
 * Created by HeYanLe on 2021/9/19 10:38.
 * https://github.com/heyanLE
 */

fun <T>  oksp (key: String,
          defValue: T): OKSP<T>{
    return OKSP<T>(key, defValue)
}

class OKSP <T> (
    private val key: String,
    private val defValue: T) {

    companion object{
        const val SP_NAME = "EasyBangumi.SP"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EasyApplication.INSTANCE.getSharedPreferences(
            SP_NAME,
            Context.MODE_PRIVATE
        )
    }

    @Volatile private var value:T? = null


    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value ?:
        (when(defValue){
            is String -> sharedPreferences.getString(key, defValue)?:defValue.toString()
            is Int -> sharedPreferences.getInt(key, defValue)
            is Long -> sharedPreferences.getLong(key, defValue)
            is Float -> sharedPreferences.getFloat(key, defValue)
            is Boolean -> sharedPreferences.getBoolean(key, defValue)
            else -> sharedPreferences.getString(key, defValue.toString())?:defValue.toString()
        } as T).also {
            value = it
        }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
            = sharedPreferences.edit().apply {
        this@OKSP.value = value
        when(value){
            is String -> putString(key, value)?:defValue.toString()
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            else -> putString(key, value.toString())?:defValue.toString()
        }
    }.apply()
}