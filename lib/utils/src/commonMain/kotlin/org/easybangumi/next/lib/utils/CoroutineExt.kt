package org.easybangumi.next.lib.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

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

fun <T> Continuation<T>.safeResume(t: T) {
    try {
        resume(t)
    } catch (e: Throwable) {
        // Ignore the exception, it may be caused by the continuation being cancelled
        // or already resumed.
        e.printStackTrace()
    }
}

fun CoroutineScope.safeCancel() {
    try {
        this.cancel()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}