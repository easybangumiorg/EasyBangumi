package org.easybangumi.next.rhino

import kotlinx.coroutines.CoroutineDispatcher
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.slf4j.Logger

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
class RhinoServiceImpl : RhinoService {
    override fun getSingletonDispatcher(): CoroutineDispatcher {
        return coroutineProvider.newSingle()
    }

    override fun getLogger(tag: String): Logger {
        return logger(tag).getSlf4jLogger()
    }
}