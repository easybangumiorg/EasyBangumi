package org.easybangumi.next.kcef

import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder
import dev.datlag.kcef.KCEFClient
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.cef.CefApp
import org.slf4j.Logger
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.getValue

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

object KcefManager {
    private val CefAppLocal = ThreadLocal<CefApp>()
    private val logger: Logger by KcefService.logger("KcefManager")
    private val init = AtomicBoolean(false)
    private val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + KcefService.service.getSingletonDispatcher() + CoroutineName("Jcef"))
    }


    fun runWithKcefClient(
        block:suspend (KCEFClient) -> Unit
    ){
        scope.launch {
            logger.info("Running block on JCEF context")
            initKCEFIfNeed()
            try {
                block(KCEF.newClient())
            } catch (e: Throwable) {
                logger.error("Error running block on JCEF context", e)
            }

        }
    }

    private suspend fun initKCEFIfNeed() {
        if (init.compareAndSet(false, true)) {
            var builder = KCEFBuilder()
            builder = KcefService.service.onKcefInit(builder)
            KCEF.init(
                builder,
                onError = {},
            )
        }

    }
}