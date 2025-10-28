package org.easybangumi.next.lib.utils

import kotlinx.datetime.Clock

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
interface MutableDataRepository <T: Any>: DataRepository<T> {

    suspend fun update(
        data: T,
        isCache: Boolean = false,
        timestamp: Long = Clock.System.now().toEpochMilliseconds()
    )

}