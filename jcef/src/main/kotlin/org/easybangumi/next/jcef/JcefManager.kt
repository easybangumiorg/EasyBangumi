package org.easybangumi.next.jcef

import com.jetbrains.cef.JCefAppConfig
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.cef.CefApp
import org.cef.misc.CefLog
import org.slf4j.Logger
import java.awt.EventQueue
import java.util.concurrent.atomic.AtomicBoolean

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
    @Volatile
    private var isInitialized = false
    private val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + JcefService.service.getSingletonDispatcher() + CoroutineName("Jcef"))
    }

    fun runOnJcefContext(
        block: (CefApp) -> Unit
    ){
        EventQueue.invokeLater {
            logger.info("Running block on JCEF context")
            val app = innerGetCefApp()
            if (!isInitialized) {

            }
            try {
                block(app)
            } catch (e: Throwable) {
                logger.error("Error running block on JCEF context", e)
            }
        }
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
            var config = JCefAppConfig.getInstance()

            config = JcefService.service.onJcefInit(config)
            try {
                CefLog.init(config.cefSettings)
                CefApp.startup(config.appArgs)
                val app = CefApp.getInstance(config.appArgs, config.cefSettings)
                app.onInitialization {
                    if (it == CefApp.CefAppState.INITIALIZED) {
                        logger.info("JCEF initialized successfully.")
                        isInitialized = true
                    } else {
                        logger.error("JCEF initialization failed.")
                    }
                }
                return app
            } catch (e: Exception) {
                logger.error("Failed to initialize JCEF", e)
                throw e
            }
        }
        throw IllegalStateException("JCEF is already initialized.")
    }

}