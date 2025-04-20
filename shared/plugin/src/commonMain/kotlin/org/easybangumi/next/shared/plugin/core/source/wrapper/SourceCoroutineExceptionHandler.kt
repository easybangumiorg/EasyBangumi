package org.easybangumi.next.shared.plugin.core.source.wrapper

import kotlinx.coroutines.CoroutineExceptionHandler
import org.easybangumi.next.shared.plugin.api.source.Source
import kotlin.coroutines.CoroutineContext

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

class SourceCoroutineExceptionHandler(
    private val source: Source
): CoroutineExceptionHandler {

    companion object : CoroutineContext.Key<SourceCoroutineExceptionHandler>

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // TODO safe mode? dialog?
    }

    override val key: CoroutineContext.Key<*> = SourceCoroutineExceptionHandler
}