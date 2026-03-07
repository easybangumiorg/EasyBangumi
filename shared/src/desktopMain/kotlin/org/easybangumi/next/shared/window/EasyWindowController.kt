package org.easybangumi.next.shared.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.easybangumi.next.jcef.JcefBrowserWindowEndpoint
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.NavigationWindowMode
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
import java.awt.Component
import java.awt.Dimension
import java.util.concurrent.FutureTask
import javax.swing.JPanel
import javax.swing.SwingUtilities

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

    private const val JCEF_HOST_PANEL_NAME = "easybangumi-jcef-host-panel"
    private const val MAIN_WINDOW_TAG = "main-window"

    val mainWindowState = EasyWindowState(
        WindowState(),
        initPage = RouterPage.DEFAULT,
        tag = MAIN_WINDOW_TAG,
        singlePageMode = false,
    )
    private val windowStateList = mutableStateListOf(
        mainWindowState
    )
    private var exitApplication: (() -> Unit)? = null
    private val logger = logger("EasyWindowController")
    private var windowTagCounter = 0L

    private val jcefLock = Any()
    private val pendingBrowserComponentSet = linkedSetOf<Component>()
    private val attachedBrowserComponentSet = linkedSetOf<Component>()

    @Volatile
    private var mainComposeWindow: ComposeWindow? = null

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
        if (windowState.tag.isBlank()) {
            windowState.tag = nextWindowTag()
        }
        windowStateList.add(windowState)
    }

    fun removeWindowState(windowState: EasyWindowState) {
        windowState.navController = null
        windowStateList.remove(windowState)
        if (windowStateList.isEmpty()) {
            exitApplication?.invoke()
        }
        if (windowState == mainWindowState) {
            exitApplication?.invoke()
        }
    }

    fun bindNavController(
        windowState: EasyWindowState,
        navController: NavHostController,
    ) {
        windowState.navController = navController
    }

    fun unbindNavController(
        windowState: EasyWindowState,
        navController: NavHostController,
    ) {
        if (windowState.navController === navController) {
            windowState.navController = null
        }
    }

    fun navigate(
        sourceNavController: NavHostController,
        routerPage: RouterPage,
        mode: NavigationWindowMode,
    ) {
        when (mode) {
            NavigationWindowMode.MainWindow -> {
                navigateInWindow(mainWindowState, routerPage, replaceCurrentPage = false)
            }

            NavigationWindowMode.CurrentWindow -> {
                val currentWindowState = findWindowStateByNavController(sourceNavController) ?: mainWindowState
                navigateInWindow(
                    windowState = currentWindowState,
                    routerPage = routerPage,
                    replaceCurrentPage = currentWindowState.singlePageMode,
                )
            }

            is NavigationWindowMode.FixedWindow -> {
                val target = findWindowStateByTag(mode.tag)
                if (target != null) {
                    target.singlePageMode = true
                    navigateInWindow(target, routerPage, replaceCurrentPage = true)
                } else {
                    addWindowState(
                        EasyWindowState(
                            state = WindowState(),
                            initPage = routerPage,
                            tag = mode.tag,
                            singlePageMode = true,
                        )
                    )
                }
            }
        }
    }

    fun closeWindowWhenBackStackExhausted(navController: NavHostController) {
        val windowState = findWindowStateByNavController(navController) ?: return
        if (windowState != mainWindowState) {
            removeWindowState(windowState)
        }
    }

    private fun navigateInWindow(
        windowState: EasyWindowState,
        routerPage: RouterPage,
        replaceCurrentPage: Boolean,
    ) {
        val navController = windowState.navController
        if (navController == null) {
            windowState.initPage = routerPage
            return
        }
        if (replaceCurrentPage) {
            val startDestinationId = navController.graph.startDestinationId
            (navController as NavController).navigate(routerPage) {
                if (startDestinationId != 0) {
                    popUpTo(startDestinationId) {
                        inclusive = true
                    }
                }
                launchSingleTop = true
            }
            return
        }
        (navController as NavController).navigate(routerPage)
    }

    private fun findWindowStateByNavController(navController: NavHostController): EasyWindowState? {
        return windowStateList.find {
            it.navController === navController
        }
    }

    private fun findWindowStateByTag(tag: String): EasyWindowState? {
        return windowStateList.find {
            it.tag == tag
        }
    }

    private fun nextWindowTag(): String {
        windowTagCounter += 1
        return "window-$windowTagCounter"
    }

    private fun bindMainWindow(window: ComposeWindow) {
        synchronized(jcefLock) {
            mainComposeWindow = window
        }

        // If the JCEF host panel is already composed, flush pending browser components.
        tryFlushPendingBrowserComponents()
    }

    private fun unbindMainWindow(window: ComposeWindow) {
        synchronized(jcefLock) {
            if (mainComposeWindow !== window) {
                return
            }
            mainComposeWindow = null
            if (attachedBrowserComponentSet.isNotEmpty()) {
                pendingBrowserComponentSet.addAll(attachedBrowserComponentSet)
                attachedBrowserComponentSet.clear()
            }
        }
    }

    private fun attachBrowserComponentToMainWindow(browserComponent: Component): Boolean {
        val hostPanel = synchronized(jcefLock) {
            val panel = jcefHostPanel
            if (panel == null) {
                // Host panel is not ready yet. Keep it pending but report success so JCEF can continue.
                pendingBrowserComponentSet.add(browserComponent)
            }
            panel
        }

        if (hostPanel == null) {
            return true
        }

        val attached = attachBrowserComponentToHostPanelNow(hostPanel, browserComponent)
        synchronized(jcefLock) {
            if (attached) {
                attachedBrowserComponentSet.add(browserComponent)
            } else {
                pendingBrowserComponentSet.add(browserComponent)
            }
        }
        return attached
    }

    private fun attachBrowserComponentToHostPanelNow(hostPanel: JPanel, browserComponent: Component): Boolean {
        return runCatching {
            runOnEdtSync {
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

    private fun bindJcefHostPanel(panel: JPanel) {
        synchronized(jcefLock) {
            jcefHostPanel = panel
        }
        tryFlushPendingBrowserComponents()
    }

    private fun unbindJcefHostPanel(panel: JPanel) {
        val shouldClear = synchronized(jcefLock) {
            if (jcefHostPanel !== panel) {
                false
            } else {
                jcefHostPanel = null
                if (attachedBrowserComponentSet.isNotEmpty()) {
                    pendingBrowserComponentSet.addAll(attachedBrowserComponentSet)
                    attachedBrowserComponentSet.clear()
                }
                true
            }
        }

        if (shouldClear) {
            runCatching {
                runOnEdtSync {
                    panel.removeAll()
                    panel.revalidate()
                    panel.repaint()
                }
            }.onFailure {
                logger.warn("Failed to clear JCEF host panel", it)
            }
        }
    }

    private fun tryFlushPendingBrowserComponents() {
        val hostPanel = synchronized(jcefLock) { jcefHostPanel } ?: return

        val pendingComponents = synchronized(jcefLock) {
            if (pendingBrowserComponentSet.isEmpty()) {
                emptyList()
            } else {
                pendingBrowserComponentSet.toList().also { pendingBrowserComponentSet.clear() }
            }
        }

        pendingComponents.forEach { browserComponent ->
            if (attachBrowserComponentToHostPanelNow(hostPanel, browserComponent)) {
                synchronized(jcefLock) {
                    attachedBrowserComponentSet.add(browserComponent)
                }
            } else {
                synchronized(jcefLock) {
                    pendingBrowserComponentSet.add(browserComponent)
                }
            }
        }
    }

    @Composable
    private fun MainWindowJcefHost() {
        val hostPanel = remember {
            JPanel(BorderLayout()).apply {
                name = JCEF_HOST_PANEL_NAME
                isOpaque = false
                isVisible = true
                minimumSize = Dimension(1, 1)
                preferredSize = Dimension(1, 1)
            }
        }

        DisposableEffect(hostPanel) {
            bindJcefHostPanel(hostPanel)
            onDispose {
                unbindJcefHostPanel(hostPanel)
            }
        }

        // Keep the panel displayable by attaching it to the main Compose window.
        // It stays effectively invisible and serves as a stable Swing parent for JCEF.
        SwingPanel(
            factory = { hostPanel },
            modifier = Modifier.size(1.dp),
        )
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
                        key(state.tag) {
                            Window(
                                onCloseRequest = {
                                    removeWindowState(state)
                                },
                                state = state.state,
                                title = "纯纯看番 Next",
                            ) {
                                val navController = rememberNavController()
                                DisposableEffect(state, navController) {
                                    bindNavController(state, navController)
                                    onDispose {
                                        unbindNavController(state, navController)
                                    }
                                }
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
                                    Box(Modifier.fillMaxSize()) {
                                        if (state == mainWindowState) {
                                            MainWindowJcefHost()
                                        }
                                        Router(
                                            navController = navController,
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

    }

}
