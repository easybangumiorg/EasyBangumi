package org.easybangumi.next.jcef

import com.jetbrains.cef.JCefAppConfig
import kotlinx.coroutines.*
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.misc.CefLog
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

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

    private const val CEF_INIT_TIMEOUT = 10000L // 10 seconds

    private val CefAppLocal = ThreadLocal<CefApp>()
    private val logger by lazy {
        JcefManager.logger()
    }
    private val singleScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.newSingle() + CoroutineName("JcefSingle"))
    }
    private val ioScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.io() + CoroutineName("JcefIo"))
    }
    private val init = AtomicBoolean(false)
    @Volatile
    private var isInitialized = false
    @Volatile
    private var isError = false

    sealed class CefAppState {
        object Error : CefAppState()
        object Initializing : CefAppState()
        data class Initialized(val cefApp: CefApp) : CefAppState()
    }

    fun runOnJcefContext(
        waitingForInit: Boolean = true,
        block: (CefAppState) -> Unit
    ){
        // EventQueue.invokeLater
        // deepseek 说一定要在 awt 线程，但实际上自测可以不用？先试试
        singleScope.launch {
//            logger.info("Running block on JCEF context")
            try {
                val app = innerGetCefApp()
                if (isError || app == null) {
                    block(CefAppState.Error)
                } else if (isInitialized) {
                    block(CefAppState.Initialized(app))
                } else if (waitingForInit) {
                    ioScope.launch {
                        val startTime = System.currentTimeMillis()
                        while(System.currentTimeMillis() - startTime < CEF_INIT_TIMEOUT) {
                            if (isInitialized) {
                                block(CefAppState.Initialized(app))

                                return@launch
                            } else if (isError) {
                                block(CefAppState.Error)
                                return@launch
                            }
                            delay(500) // Check every 500ms
                        }

                        if (isInitialized) {
                            block(CefAppState.Initialized(app))
                        } else if (isError) {
                            block(CefAppState.Error)
                        } else {
                            block(CefAppState.Initializing)
                        }
                    }
                } else {
                    block(CefAppState.Initializing)
                }
            } catch (e: Throwable) {
                logger.error("Error running block on JCEF context", e)
            }

        }
    }

    private fun innerGetCefApp(): CefApp? {
        val cefApp = CefAppLocal.get()
        if (cefApp != null) {
            return cefApp
        }
        val newApp = initCefApp()
        if (newApp != null) {
            CefAppLocal.set(newApp)
        }
        return newApp
    }

    private fun initCefApp(): CefApp? {
        if (init.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(
                thread(start = false) {
                    runOnJcefContext(false) {
                        if (it is CefAppState.Initialized) {
                            it.cefApp.dispose()
                        }
                    }
                },
            )
            try {
                val config = makeJcefConfig()
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
                return null
            }
        }
        throw IllegalStateException("JCEF is already initialized.")
    }

    private fun makeJcefConfig(): JCefAppConfig {
        val config = JCefAppConfig.getInstance()
        config.appArgsAsList.apply {
            add("--autoplay-policy=no-user-gesture-required")
            add("--mute-audio")
        }

        config.cefSettings.windowless_rendering_enabled = true
        config.cefSettings.cache_path = getCachePath()
        config.cefSettings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_DEFAULT
        config.cefSettings.log_file = getLogPath()
        return config
    }



    // jcef 如果泄露则会出现 cache 文件夹被占用，每次都使用新路径，并饱和删除所有路径
    private fun getCachePath(): String {
        val path = pathProvider.getCacheJvmPath("jcef")
        val file = File(path)
        try {
            file.deleteRecursively()
        }catch (e: Exception) {
            // Ignore any errors during deletion
        }
        file.mkdirs()
        val configFile = File(file, System.currentTimeMillis().toString())
        configFile.mkdirs()
        return configFile.absolutePath
    }

    private fun getLogPath(): String {
        val path = pathProvider.getFileJvmPath("jcef")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(System.currentTimeMillis())
        val logFile = File(path, "jcef-$date.log")
        logFile.parentFile.mkdirs()
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        return logFile.absolutePath
    }

}