package org.easybangumi.next.lib.utils

import java.util.concurrent.ConcurrentHashMap

actual fun <K, V> safeMutableMapOf(vararg value: Pair<K, V>): MutableMap<K, V> {
    return ConcurrentHashMap<K, V>(mapOf(*value))
}