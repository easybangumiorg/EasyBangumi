package org.easybangumi.next.lib.unifile

import okio.Path
import okio.Path.Companion.toPath
import org.easybangumi.next.lib.unifile.core.OkioUniFile
import java.io.File

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



actual fun UniFileFactory.fromUFD(ufd: UFD): UniFile? {
    return when (ufd.type) {
        UFD.TYPE_JVM -> {
            JvmUniFile(File(ufd.uri))
        }
        UFD.TYPE_OKIO -> {
            OkioUniFile(path = ufd.uri.toPath())
        }
        else -> {
            null
        }
    }
}