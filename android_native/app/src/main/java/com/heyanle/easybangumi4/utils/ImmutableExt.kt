package com.heyanle.easybangumi4.utils

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet

/**
 * Created by heyanlin on 2024/2/19 15:26.
 */

fun <T> Collection<T>.toImmutableList(): ImmutableList<T> {
    return ImmutableList.copyOf(this)
}

fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V> {
    return ImmutableMap.copyOf(this)
}

fun <V> Collection<V>.toImmutableSet(): ImmutableSet<V> {
    return ImmutableSet.copyOf(this)
}

