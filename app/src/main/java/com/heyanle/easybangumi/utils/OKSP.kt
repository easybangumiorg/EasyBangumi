package com.heyanle.easybangumi.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.ArraySet
import com.heyanle.easybangumi.EasyApplication
import okhttp3.internal.wait
import java.lang.Exception
import kotlin.reflect.KProperty

/**
 * Created by HeYanLe on 2021/9/19 10:38.
 * https://github.com/heyanLE
 */

fun <T> oksp (key: String,
          defValue: T): OKSP<T>{
    return OKSP<T>(key, defValue)
}

class OKSPSet <T> (
    private val key: String,
    private val clazz: Class<T>) : MutableSet<T>{

    companion object{
        const val SP_NAME = "EasyBangumi.SP"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EasyApplication.INSTANCE.getSharedPreferences(
            OKSP.SP_NAME,
            Context.MODE_PRIVATE
        )
    }

    @Volatile private var value:MutableSet<T> = hashSetOf()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableSet<T> {
        load()
        return this
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableSet<T>){
        value.clear()
        value.addAll(value)
        save()
    }

    private fun load(){
        val ss = sharedPreferences.getStringSet(key, emptySet())?: emptySet()
        value.clear()

        for(s in ss){
            clazz.cast(s)?.let {
                value.add(it)
            }
        }
    }

    private fun save(){

    }

    override val size: Int
        get() = value.size

    override fun contains(element: T): Boolean {
        return value.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return value.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun iterator(): MutableIterator<T> {
        throw OKSPException()
    }

    override fun add(element: T): Boolean {
        return value.add(element).apply {
            save()
        }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return value.addAll(elements).apply {
            save()
        }
    }

    override fun clear() {
        value.clear()
        save()
    }

    override fun remove(element: T): Boolean {
        return value.remove(element).apply {
            save()
        }

    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return value.removeAll(elements).apply {
            save()
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        return value.retainAll(elements).apply {
            save()
        }
    }
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

class OKSPException: Exception("Can't iterator from OKSPSet, please use getValueSet() get set first")