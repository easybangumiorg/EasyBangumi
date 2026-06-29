package org.easybangumi.next.lib.store.cache

import com.mayakapps.kache.ContainerKache
import com.mayakapps.kache.FileKache
import com.mayakapps.kache.FileKache.Configuration
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD

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

private val logger = logger("FileKacheHelper")

suspend fun FileKache(
    ufd: UFD,
    maxSize: Long,
    configuration: Configuration.() -> Unit = {},
): ContainerKache<String, String>? {
    if (ufd.type == UFD.TYPE_JVM || ufd.type == UFD.TYPE_OKIO) {
        logger.info(ufd.toString())
        return FileKache(ufd.uri, maxSize, configuration)
    }
    return null
}
