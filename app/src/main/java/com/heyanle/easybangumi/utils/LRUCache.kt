package com.heyanle.easybangumi.utils

/**
 * Created by HeYanLe on 2022/6/12 0:38.
 * https://github.com/heyanLE
 */
class LRUCache<K, V> (
    private val capacity: Int
): LinkedHashMap<K,V>() {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > capacity
    }


}