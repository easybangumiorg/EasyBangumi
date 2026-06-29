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
actual class WeakRef<out T : Any> actual constructor(target: T) {
    private val underlying: java.lang.ref.WeakReference<T> = java.lang.ref.WeakReference(target)
    actual val targetOrNull: T? get() = underlying.get()
}