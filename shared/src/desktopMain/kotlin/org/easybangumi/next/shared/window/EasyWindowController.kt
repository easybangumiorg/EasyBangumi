package org.easybangumi.next.shared.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.launch
import org.easybangumi.next.jcef.JcefBrowserWindowEndpoint
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.NeedKnowDialog
import org.easybangumi.next.shared.Router
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.compose.UI
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.image.LocalImageLoader
import org.easybangumi.next.shared.foundation.image.createImageLoader
import org.easybangumi.next.shared.foundation.snackbar.MoeSnackBar
import org.easybangumi.next.shared.scheme.LocalSizeScheme
import org.easybangumi.next.shared.scheme.SizeScheme
import org.easybangumi.next.shared.theme.EasyTheme
import org.koin.compose.KoinContext
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.util.concurrent.FutureTask
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.JWindow

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
object EasyWindowController {

    private const val JCEF_HOST_WINDOW_NAME = "easybangumi-jcef-host-window"
    private const val JCEF_HOST_PANEL_NAME = "easybangumi-jcef-host-panel"

    val mainWindowState = EasyWindowState(
        WindowState(),
        RouterPage.DEFAULT
    )
    private val windowStateList = mutableStateListOf(
        mainWindowState
    )
    private var exitApplication: (() -> Unit)? = null
    private val logger = logger("EasyWindowController")

    private val jcefLock = Any()
    private val pendingBrowserComponentSet = linkedSetOf<Component>()
    private val attachedBrowserComponentSet = linkedSetOf<Component>()

    @Volatile
    private var mainComposeWindow: ComposeWindow? = null

    @Volatile
    private var jcefHostWindow: JWindow? = null

    @Volatile
    private var jcefHostPanel: JPanel? = null

    init {
        JcefBrowserWindowEndpoint.register(
            addBrowserToWindow = { browserComponent ->
                attachBrowserComponentToMainWindow(browserComponent)
            },
            removeBrowserFromWindow = { browserComponent ->
                detachBrowserComponentFromWindow(browserComponent)
            },
        )
    }

    fun bindExitApplication(
        exitApplication: () -> Unit
    ) {
        this.exitApplication = exitApplication
    }

    fun addWindowState(windowState: EasyWindowState) {
        coroutineProvider.globalScope().launch(
            coroutineProvider.io()
        ) {
            windowStateList.add(windowState)
        }

    }

    fun removeWindowState(windowState: EasyWindowState) {
        windowStateList.remove(windowState)
        if (windowStateList.isEmpty()) {
            exitApplication?.invoke()
        }
        if (windowState == mainWindowState) {
            exitApplication?.invoke()
        }
    }

//    fun findWindowStateByNavController(
//        navController: NavHostController
//    ): EasyWindowState? {
//        return windowStateList.find {
//            it.navController == navController
//        }
//    }

    private fun bindMainWindow(window: ComposeWindow) {
        synchronized(jcefLock) {
            mainComposeWindow = window
        }

        ensureJcefHostPanel(window)

        val pendingComponents = synchronized(jcefLock) {
            if (pendingBrowserComponentSet.isEmpty()) {
                emptyList()
            } else {
                pendingBrowserComponentSet.toList().also { pendingBrowserComponentSet.clear() }
            }
        }

        pendingComponents.forEach { browserComponent ->
            if (attachBrowserComponentToMainWindowNow(window, browserComponent)) {
                synchronized(jcefLock) {
                    attachedBrowserComponentSet.add(browserComponent)
                }
            } else {
                synchronized(jcefLock) {
                    if (mainComposeWindow === window) {
                        pendingBrowserComponentSet.add(browserComponent)
                    }
                }
            }
        }
    }

    private fun unbindMainWindow(window: ComposeWindow) {
        val shouldDispose = synchronized(jcefLock) {
            if (mainComposeWindow !== window) {
                false
            } else {
                mainComposeWindow = null
                if (attachedBrowserComponentSet.isNotEmpty()) {
                    pendingBrowserComponentSet.addAll(attachedBrowserComponentSet)
                    attachedBrowserComponentSet.clear()
                }
                true
            }
        }

        if (shouldDispose) {
            disposeJcefHostWindow()
        }
    }

    private fun attachBrowserComponentToMainWindow(browserComponent: Component): Boolean {
        val currentWindow = synchronized(jcefLock) {
            val window = mainComposeWindow
            if (window == null) {
                pendingBrowserComponentSet.add(browserComponent)
            }
            window
        }

        if (currentWindow == null) {
            return true
        }

        val attached = attachBrowserComponentToMainWindowNow(currentWindow, browserComponent)
        synchronized(jcefLock) {
            if (attached) {
                attachedBrowserComponentSet.add(browserComponent)
            } else {
                if (mainComposeWindow === currentWindow) {
                    pendingBrowserComponentSet.add(browserComponent)
                }
            }
        }
        return attached
    }

    private fun attachBrowserComponentToMainWindowNow(window: ComposeWindow, browserComponent: Component): Boolean {
        return runCatching {
            runOnEdtSync {
                val hostPanel = ensureJcefHostPanel(window)
                browserComponent.parent?.remove(browserComponent)
                hostPanel.add(browserComponent, BorderLayout.CENTER)
                browserComponent.isVisible = true
                hostPanel.revalidate()
                hostPanel.repaint()
            }
            true
        }.getOrElse {
            logger.error("Failed to attach JCEF browser component to window", it)
            false
        }
    }

    private fun detachBrowserComponentFromWindow(browserComponent: Component) {
        synchronized(jcefLock) {
            pendingBrowserComponentSet.remove(browserComponent)
            attachedBrowserComponentSet.remove(browserComponent)
        }

        runCatching {
            runOnEdtSync {
                val parent = browserComponent.parent ?: return@runOnEdtSync
                parent.remove(browserComponent)
                parent.revalidate()
                parent.repaint()
            }
        }.onFailure {
            logger.warn("Failed to detach JCEF browser component from window", it)
        }
    }

    private fun ensureJcefHostPanel(window: ComposeWindow): JPanel = runOnEdtSync {
        val currentWindow = jcefHostWindow
        val currentPanel = jcefHostPanel
        if (currentWindow != null && currentPanel != null && currentWindow.isDisplayable) {
            return@runOnEdtSync currentPanel
        }

        currentWindow?.let {
            runCatching {
                it.isVisible = false
                it.dispose()
            }
        }

        val hostPanel = JPanel(BorderLayout()).apply {
            name = JCEF_HOST_PANEL_NAME
            isOpaque = false
            isVisible = true
            preferredSize = Dimension(1080, 1080)
            minimumSize = Dimension(1, 1)
            maximumSize = Dimension(1080, 1080)
        }

        val hostWindow = JWindow().apply {
            name = JCEF_HOST_WINDOW_NAME
            setFocusableWindowState(false)
            isAutoRequestFocus = false
            isAlwaysOnTop = false
            background = Color(0, 0, 0, 0)
            contentPane.layout = BorderLayout()
            contentPane.add(hostPanel, BorderLayout.CENTER)
            setSize(1080, 1080)
            setLocation(window.x - 10000, window.y - 10000)
            isVisible = true
        }

        jcefHostWindow = hostWindow
        jcefHostPanel = hostPanel
        hostPanel
    }

    private fun disposeJcefHostWindow() {
        runCatching {
            runOnEdtSync {
                jcefHostPanel?.removeAll()
                jcefHostPanel = null
                jcefHostWindow?.let {
                    it.isVisible = false
                    it.dispose()
                }
                jcefHostWindow = null
            }
        }.onFailure {
            logger.warn("Failed to dispose JCEF host window", it)
        }
    }

    private fun <T> runOnEdtSync(block: () -> T): T {
        if (SwingUtilities.isEventDispatchThread()) {
            return block()
        }
        val task = FutureTask {
            block()
        }
        SwingUtilities.invokeAndWait(task)
        return task.get()
    }

    @Composable
    fun EasyWindowHost() {
        KoinContext() {
            CompositionLocalProvider(
                LocalImageLoader provides createImageLoader(),

            ) {
                EasyTheme {
                    windowStateList.forEach { state ->
                        Window(
                            onCloseRequest = {
                                removeWindowState(state)
                            },
                            state = state.state,
                            title = "纯纯看番 Next",
                        ) {
                            if (state == mainWindowState) {
                                DisposableEffect(window) {
                                    bindMainWindow(window)
                                    onDispose {
                                        unbindMainWindow(window)
                                    }
                                }
                            }
                            val top = with(LocalDensity.current) {
                                WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
                            }
                            CompositionLocalProvider(
                                LocalEasyWindowState provides state,
                                LocalUIMode provides UI.getUiMode(),
                                LocalSizeScheme provides SizeScheme(statusBarHeight = top)
                            ) {
                                Router(
                                    initRoute = state.initPage ?: RouterPage.DEFAULT,
                                )
                                MoeSnackBar()
                                if (state == mainWindowState) {
                                    NeedKnowDialog()
                                }
                            }

                        }
                    }
                }
            }
        }

    }

}
