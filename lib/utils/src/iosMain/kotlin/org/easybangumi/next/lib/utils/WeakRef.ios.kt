package org.easybangumi.next.lib.utils

import kotlin.experimental.ExperimentalNativeApi

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
@OptIn(ExperimentalNativeApi::class)
actual class WeakRef<out T : Any> actual constructor(target: T) {
    private val underlying: kotlin.native.ref.WeakReference<T> = kotlin.native.ref.WeakReference(target)
    actual val targetOrNull: T? get() = underlying.get()
}