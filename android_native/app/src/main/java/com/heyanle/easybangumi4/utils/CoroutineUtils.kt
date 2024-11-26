package com.heyanle.easybangumi4.utils

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Created by heyanlin on 2024/6/24.
 */

fun <T> Continuation<T>.safeResume(t: T) {
    kotlin.runCatching {
        this.resume(t)
    }.onFailure {
        it.printStackTrace()
    }
}