package com.heyanle.easybangumi.utils.oksp

import kotlin.reflect.KProperty

/**
 * Created by HeYanLe on 2021/10/10 15:51.
 * https://github.com/heyanLE
 */
class OKSPSetProxy<T>(
    private val oksp: OKSP,
    private val key: String,
    private val encoder: OKSPEncoder<T>,
    private val decoder: OKSPDecoder<T>,
): MutableSet<T>{
    @Volatile private var value:MutableSet<T> = hashSetOf()
    private val okspSet: OKSPSet<T> by lazy {
        OKSPSet(this)
    }
    private fun save(){
        val ss = mutableSetOf<String>()
        for(t in value){
            ss.add(encoder.encode(t))
        }
        oksp.sharedPreferences.edit().putStringSet(key, ss).apply()
    }

    private fun load(){
        val ss = oksp.sharedPreferences.getStringSet(key, emptySet())?: emptySet()
        value.clear()
        for(s in ss){
            value.add(decoder.decoder(s, null))
        }
    }

    fun getSnapShootValue():MutableSet<T>{
        val res = hashSetOf<T>()
        res.addAll(value)
        return res
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): OKSPSet<T> {
        load()
        return okspSet
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

    // 为了防止歧义，需要先创建快照才可读
    override fun iterator(): MutableIterator<T> {
        throw OKSPException.cantIterator()
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
}
class OKSPSet<T>(private val okspSetProxy: OKSPSetProxy<T>): MutableSet<T>{
    override fun add(element: T): Boolean {
        return okspSetProxy.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return okspSetProxy.addAll(elements)
    }

    override fun clear() {
        okspSetProxy.clear()
    }

    override fun iterator(): MutableIterator<T> {
        return okspSetProxy.iterator()
    }

    override fun remove(element: T): Boolean {
        return okspSetProxy.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return okspSetProxy.removeAll(elements)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        return okspSetProxy.retainAll(elements)
    }

    override val size: Int
        get() = okspSetProxy.size

    override fun contains(element: T): Boolean {
        return okspSetProxy.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return okspSetProxy.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return okspSetProxy.isEmpty()
    }

    fun getSnapShootValue():MutableSet<T>{
        return okspSetProxy.getSnapShootValue()
    }
}
