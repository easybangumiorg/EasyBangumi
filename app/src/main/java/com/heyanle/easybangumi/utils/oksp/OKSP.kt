package com.heyanle.easybangumi.utils.oksp

import android.content.Context
import android.content.SharedPreferences
import java.lang.Exception

/**
 * Created by HeYanLe on 2021/10/10 15:17.
 * https://github.com/heyanLE
 */

object DefaultOksp{
    val oksp = OKSP()
}

fun initDefaultOksp(context: Context, spName: String){
    DefaultOksp.oksp.init(context, spName)
}
fun <T> okspValue(key: String, defValue: T): OKSPValue<T> {
    return DefaultOksp.oksp?.okspValue(key, defValue)?:throw OKSPException.uninitialized()
}

fun <T> okspValue(key: String, defValue: T, encoder: OKSPEncoder<T>, decoder: OKSPDecoder<T>): OKSPValue<T> {
    return DefaultOksp.oksp?.okspValue(key, defValue, encoder, decoder)?:throw OKSPException.uninitialized()
}

fun okspSet(): OkspSetGetter {
    return OkspSetGetter
}

object OkspSetGetter{
    fun string(key: String): OKSPSetProxy<String> {
        return DefaultOksp.oksp.okspSet(key)
    }
    fun int(key: String): OKSPSetProxy<Int> {
        return DefaultOksp.oksp.okspSet(key)
    }
    fun long(key: String): OKSPSetProxy<Long> {
        return DefaultOksp.oksp.okspSet(key)
    }
    fun float(key: String): OKSPSetProxy<Float> {
        return DefaultOksp.oksp.okspSet(key)
    }
    fun double(key: String): OKSPSetProxy<Double> {
        return DefaultOksp.oksp.okspSet(key)
    }
    fun boolean(key: String): OKSPSetProxy<Boolean> {
        return DefaultOksp.oksp.okspSet(key)
    }
    fun <T> other(key: String, encoder: OKSPEncoder<T>, decoder: OKSPDecoder<T>){
        return DefaultOksp.oksp.okspSetOther(key, encoder, decoder)
    }
}



class OKSP{

    private var context: Context? = null
    private var spName: String = ""

    fun init(context: Context, spName: String){
        this.context = context.applicationContext
        this.spName = spName
    }

    val sharedPreferences: SharedPreferences by lazy {
        context?.getSharedPreferences(
            spName,
            Context.MODE_PRIVATE
        )?:throw OKSPException.uninitialized()
    }

    fun <T> okspValue(key: String, defValue: T): OKSPValue<T> {
        return if(defValue is OKSPAble<*>){
            try{
                OKSPValue(this, key, defValue, defValue.getEncoder() as OKSPEncoder<T>, defValue.getDecoder()as OKSPDecoder<T>)
            }catch (e: java.lang.ClassCastException){
                throw OKSPException.castException("please use OKSPAble interface with right generics !")
            }catch (e: Exception){
                throw e
            }
        }else{
            OKSPValue(this, key, defValue)
        }

    }
    fun <T> okspValue(key: String, defValue: T, encoder: OKSPEncoder<T>, decoder: OKSPDecoder<T>): OKSPValue<T> = OKSPValue(this, key, defValue)

    fun <T> okspSetOther(key: String, encoder: OKSPEncoder<T>, decoder: OKSPDecoder<T>){
        OKSPSetProxy<T>(this, key, encoder, decoder)
    }

    inline fun <reified T> okspSet(key: String): OKSPSetProxy<T> {
        return when{
            0 is T -> {
                OKSPSetProxy<T>(this, key, object: OKSPEncoder<T> {
                    override fun encode(value: T): String {
                        return "$value"
                    }
                }, object : OKSPDecoder<T> {
                    override fun decoder(source: String, value: T?): T {
                        return source.toInt() as T
                    }
                })
            }
            0L is T -> {
                OKSPSetProxy<T>(this, key, object: OKSPEncoder<T> {
                    override fun encode(value: T): String {
                        return "$value"
                    }
                }, object : OKSPDecoder<T> {
                    override fun decoder(source: String, value: T?): T {
                        return source.toLong() as T
                    }
                })
            }
            0F is T -> {
                OKSPSetProxy<T>(this, key, object: OKSPEncoder<T> {
                    override fun encode(value: T): String {
                        return "$value"
                    }
                }, object : OKSPDecoder<T> {
                    override fun decoder(source: String, value: T?): T {
                        return source.toFloat() as T
                    }
                })
            }
            0.0 is T -> {
                OKSPSetProxy<T>(this, key, object: OKSPEncoder<T> {
                    override fun encode(value: T): String {
                        return "$value"
                    }
                }, object : OKSPDecoder<T> {
                    override fun decoder(source: String, value: T?): T {
                        return source.toDouble() as T
                    }
                })
            }
            true is T -> {
                OKSPSetProxy<T>(this, key, object: OKSPEncoder<T> {
                    override fun encode(value: T): String {
                        return "$value"
                    }
                }, object : OKSPDecoder<T> {
                    override fun decoder(source: String, value: T?): T {
                        return source.toBoolean() as T
                    }
                })
            }
            "" is T -> {
                OKSPSetProxy<T>(this, key, object: OKSPEncoder<T> {
                    override fun encode(value: T): String {
                        return value as String
                    }
                }, object : OKSPDecoder<T> {
                    override fun decoder(source: String, value: T?): T {
                        return source as T
                    }
                })
            }
            else -> {
                throw OKSPException.encoderNotFound()
            }
        }
    }

}





