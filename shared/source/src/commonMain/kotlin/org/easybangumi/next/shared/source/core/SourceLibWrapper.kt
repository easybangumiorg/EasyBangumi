package org.easybangumi.next.shared.source.core

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.source.SourceWrapper
import org.easybangumi.next.shared.source.utils.PluginPathProvider
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

class SourceLibWrapper(
    source: Source
) : SourceWrapper(source) {

    override val workPath: UFD by lazy {
        PluginPathProvider.getSourceWorkPath(source)
    }

    override val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.io() + SourceCoroutineExceptionHandler(source) + CoroutineName("source[${source.key}]"))
    }


}