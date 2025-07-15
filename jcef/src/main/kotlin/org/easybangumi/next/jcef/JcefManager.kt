package org.easybangumi.next.jcef

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.friwi.jcefmaven.CefAppBuilder
import org.cef.CefApp
import org.slf4j.Logger
import java.awt.EventQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation

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
object JcefManager {

    private val CefAppLocal = ThreadLocal<CefApp>()
    private val logger: Logger by JcefService.logger("JcefManager")
    private val init = AtomicBoolean(false)
    private val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + JcefService.service.getSingletonDispatcher() + CoroutineName("Jcef"))
    }

    fun runOnJcefContext(
        block: (CefApp) -> Unit
    ){
        EventQueue.invokeLater {
            logger.info("Running block on JCEF context")
            val app = innerGetCefApp()
            try {
                block(app)
            } catch (e: Throwable) {
                logger.error("Error running block on JCEF context", e)
            }
        }
//        scope.launch {
//
//        }
    }

    private fun innerGetCefApp(): CefApp {
        val cefApp = CefAppLocal.get()
        if (cefApp != null) {
            return cefApp
        }
        val newApp = initCefApp()
        CefAppLocal.set(newApp)
        return newApp
    }

    private fun initCefApp(): CefApp {
        if (init.compareAndSet(false, true)) {
            var builder = CefAppBuilder()
            builder = JcefService.service.onJcefInit(builder)
            val app = builder.build()
            return app
        }
        throw IllegalStateException("JCEF is already initialized.")
    }

}