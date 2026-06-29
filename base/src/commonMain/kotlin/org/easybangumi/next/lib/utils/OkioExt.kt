package org.easybangumi.next.lib.utils

import okio.Buffer
import okio.Source
import okio.Sink
import okio.buffer

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

fun Source.copyTo(sink: Sink) {
    var bytesRead: Long
    val buffer = Buffer()
    do {
        bytesRead = read(buffer, 8192)
        if (bytesRead != -1L) {
            sink.write(buffer, bytesRead)
        }
    } while (bytesRead != -1L)
    sink.flush()
}