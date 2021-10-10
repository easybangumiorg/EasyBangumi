package com.heyanle.easybangumi.utils.oksp

import kotlin.reflect.KProperty

/**
 * Created by HeYanLe on 2021/10/10 15:26.
 * https://github.com/heyanLE
 */
class OKSPValue<T>(
    private val oksp: OKSP,
    private val key: String,
    private val defValue: T,
    private val encoder: OKSPEncoder<T>? = null,
    private val decoder: OKSPDecoder<T>? = null,
) {

    @Volatile private var value:T? = null

    private fun getE(): OKSPEncoder<T>{
        return encoder?:throw OKSPException.encoderNotFound()
    }

    private fun getD(): OKSPDecoder<T>{
        return decoder?:throw OKSPException.decoderNotFound()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value ?:
        (when(defValue){
            is String -> oksp.sharedPreferences.getString(key, defValue)?:defValue.toString()
            is Int -> oksp.sharedPreferences.getInt(key, defValue)
            is Long -> oksp.sharedPreferences.getLong(key, defValue)
            is Float -> oksp.sharedPreferences.getFloat(key, defValue)
            is Boolean -> oksp.sharedPreferences.getBoolean(key, defValue)
            else -> {
                val defString = getE().encode(defValue)
                val ss = oksp.sharedPreferences.getString(key, defString)?:defString
               getD().decoder(ss, value)
            }
        } as T).also {
            value = it
        }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
            = oksp.sharedPreferences.edit().apply {
        this@OKSPValue.value = value
        when(value){
            is String -> putString(key, value)?:defValue.toString()
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            else -> putString(key, getE().encode(value))?:defValue.toString()
        }
    }.apply()

}