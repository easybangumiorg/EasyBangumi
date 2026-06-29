package org.easybangumi.next.lib.utils


/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
// 带有两个 key 的 map
class MultiMap<K1, K2, V> {

    private val map = hashMapOf<K1, HashMap<K2, V>>()

    fun put(k1: K1, k2: K2, v: V) {
        map[k1]?.put(k2, v) ?: run {
            map[k1] = hashMapOf(k2 to v)
        }
    }
    
    fun get(k1: K1, k2: K2): V? {
        return map[k1]?.get(k2)
    }

    fun remove(k1: K1, k2: K2) {
        map[k1]?.remove(k2)
    }
    
    fun clear() {
        map.clear()
    }

    fun contains(k1: K1, k2: K2): Boolean {
        return map[k1]?.contains(k2) ?: false
    }

    fun iterator(): MutableIterator<MutableMap.MutableEntry<K1, HashMap<K2, V>>> {
        return map.iterator()
    }

    fun keys(): Set<K1> {
        return map.keys
    }

    fun values(): List<V> {
        return map.values.flatMap { it.values }
    }

    
    

}