package org.easybangumi.next.jcef

import com.jetbrains.cef.JCefAppConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefRequestContext
import org.cef.misc.CefLog
import org.cef.network.CefCookieManager
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
    private const val CEF_INIT_POLL_INTERVAL = 200L
    private const val COOKIE_FLUSH_TIMEOUT = 3000L
    private const val SHUTDOWN_WAIT_TIMEOUT = 5000L

    private val CefAppLocal = ThreadLocal<CefApp>()
    private val logger by lazy {
        JcefManager.logger()
    }
    private val singleScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.newSingle() + CoroutineName("JcefSingle"))
    }
    private val init = AtomicBoolean(false)
    private val requestContextLock = Any()

    @Volatile
    private var sharedRequestContext: CefRequestContext? = null

    @Volatile
    private var sharedCookieManager: CefCookieManager? = null

    @Volatile
    private var jcefThread: Thread? = null

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
    ) {
        singleScope.launch {
            jcefThread = Thread.currentThread()
            try {
                val app = innerGetCefApp()
                if (isError || app == null) {
                    block(CefAppState.Error)
                } else if (isInitialized) {
                    block(CefAppState.Initialized(app))
                } else if (waitingForInit) {
                    val startTime = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startTime < CEF_INIT_TIMEOUT) {
                        if (isInitialized) {
                            block(CefAppState.Initialized(app))
                            return@launch
                        } else if (isError) {
                            block(CefAppState.Error)
                            return@launch
                        }
                        delay(CEF_INIT_POLL_INTERVAL)
                    }
                    if (isInitialized) {
                        block(CefAppState.Initialized(app))
                    } else if (isError) {
                        block(CefAppState.Error)
                    } else {
                        block(CefAppState.Initializing)
                    }
                } else {
                    block(CefAppState.Initializing)
                }
            } catch (e: Throwable) {
                logger.error("Error running block on JCEF context", e)
                block(CefAppState.Error)
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

    fun tryPreload() {
        runOnJcefContext(false) {}
    }

    fun getSharedRequestContext(): CefRequestContext? {
        sharedRequestContext?.let {
            return it
        }
        return synchronized(requestContextLock) {
            sharedRequestContext?.let {
                return@synchronized it
            }
            runCatching {
                CefRequestContext.getGlobalContext()
            }.onFailure {
                logger.error("Failed to get global request context", it)
            }.getOrNull()?.also {
                sharedRequestContext = it
                if (sharedCookieManager == null) {
                    sharedCookieManager = resolveCookieManagerFromContext(it)
                }
            }
        }
    }

    fun getSharedCookieManager(): CefCookieManager? {
        sharedCookieManager?.let {
            return it
        }
        val contextManager = getSharedRequestContext()?.let { resolveCookieManagerFromContext(it) }
        if (contextManager != null) {
            sharedCookieManager = contextManager
            return contextManager
        }
        return runCatching {
            CefCookieManager.getGlobalManager()
        }.onFailure {
            logger.error("Failed to get global cookie manager", it)
        }.getOrNull()?.also {
            sharedCookieManager = it
        }
    }

    fun flushCookieStoreSync(timeoutMillis: Long = COOKIE_FLUSH_TIMEOUT): Boolean {
        if (Thread.currentThread() === jcefThread) {
            val success = flushCookieStoreInternal(timeoutMillis)
            if (!success) {
                logger.warn("Cookie flush failed on JCEF thread.")
            }
            return success
        }

        val done = CountDownLatch(1)
        val success = AtomicBoolean(false)
        runOnJcefContext(true) { state ->
            if (state is CefAppState.Initialized) {
                success.set(flushCookieStoreInternal(timeoutMillis))
            }
            done.countDown()
        }
        val completed = done.await(timeoutMillis + CEF_INIT_TIMEOUT, TimeUnit.MILLISECONDS)
        if (!completed) {
            logger.warn("Timeout while waiting for cookie flush task.")
            return false
        }
        if (!success.get()) {
            logger.warn("Cookie flush finished but did not succeed.")
        }
        return success.get()
    }

    private fun initCefApp(): CefApp? {
        if (init.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(
                thread(start = false) {
                    runBlocking {
                        val shutdownDone = CompletableDeferred<Unit>()
                        runOnJcefContext(false) {
                            try {
                                if (it is CefAppState.Initialized) {
                                    flushCookieStoreInternal(COOKIE_FLUSH_TIMEOUT)
                                    it.cefApp.dispose()
                                    sharedRequestContext = null
                                    sharedCookieManager = null
                                }
                            } finally {
                                shutdownDone.complete(Unit)
                            }
                        }
                        withTimeoutOrNull(SHUTDOWN_WAIT_TIMEOUT) {
                            shutdownDone.await()
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
                        getSharedCookieManager()
                        getSharedRequestContext()
                        logger.info("JCEF initialized successfully.")
                        isInitialized = true
                    } else {
                        isError = true
                        logger.error("JCEF initialization failed.")
                    }
                }

                return app
            } catch (e: Exception) {
                isError = true
                init.set(false)
                sharedRequestContext = null
                sharedCookieManager = null
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
        config.cefSettings.persist_session_cookies = true
        config.cefSettings.persist_user_preferences = true
        config.cefSettings.cookieable_schemes_exclude_defaults = false
        return config
    }

    private fun flushCookieStoreInternal(timeoutMillis: Long): Boolean {
        runCatching {
            val cookieManager = getSharedCookieManager() ?: return false
            val latch = CountDownLatch(1)
            val shouldWait = cookieManager.flushStore {
                latch.countDown()
            }
            val completed = if (shouldWait) {
                latch.await(timeoutMillis, TimeUnit.MILLISECONDS)
            } else {
                true
            }
            if (shouldWait && !completed) {
                logger.warn("Cookie flush callback timed out after ${timeoutMillis}ms.")
            }
            return completed
        }.onFailure {
            logger.warn("Failed to flush cookie store", it)
        }
        return false
    }

    private fun resolveCookieManagerFromContext(context: CefRequestContext): CefCookieManager? {
        val manager = runCatching {
            val method = context.javaClass.methods.firstOrNull {
                it.name == "getCookieManager" && (it.parameterCount == 0 || it.parameterCount == 1)
            } ?: return@runCatching null
            when (method.parameterCount) {
                0 -> method.invoke(context) as? CefCookieManager
                1 -> method.invoke(context, null) as? CefCookieManager
                else -> null
            }
        }.onFailure {
            logger.warn("Failed to resolve cookie manager from request context", it)
        }.getOrNull()
        return manager ?: runCatching {
            CefCookieManager.getGlobalManager()
        }.onFailure {
            logger.error("Failed to fallback to global cookie manager", it)
        }.getOrNull()
    }

    private fun getCachePath(): String {
        val cacheDir = File(pathProvider.getFileJvmPath("jcef-cache"))
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            logger.warn("Failed to create JCEF cache directory: ${cacheDir.absolutePath}")
        }
        logger.info("JCEF cache path: ${cacheDir.absolutePath}")
        return cacheDir.absolutePath
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
