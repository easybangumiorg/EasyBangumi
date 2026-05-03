package org.easybangumi.next.shared.window

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.WindowPlacement
import org.easybangumi.next.shared.playcon.desktop.FullscreenStrategy
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import javax.swing.SwingUtilities

/**
 * Desktop fullscreen backend:
 * - Windows: borderless fullscreen covering taskbar
 * - macOS/others: Compose WindowPlacement fullscreen
 */
class DesktopWindowFullscreenStrategy(
    private val easyWindowStateProvider: () -> EasyWindowState?,
) : FullscreenStrategy {

    private data class WindowsFullscreenSnapshot(
        val window: ComposeWindow,
        val bounds: Rectangle,
        val extendedState: Int,
        val isUndecorated: Boolean,
        val isResizable: Boolean,
        val isAlwaysOnTop: Boolean,
    )

    private val osName = System.getProperty("os.name").lowercase()
    private val isWindows = "windows" in osName

    private var isFullscreenState by mutableStateOf(false)
    private var windowsFullscreenSnapshot: WindowsFullscreenSnapshot? = null

    override fun enterFullscreen() {
        setFullscreen(true)
    }

    override fun exitFullscreen() {
        setFullscreen(false)
    }

    fun toggleFullscreen() {
        setFullscreen(!isFullscreen())
    }

    override fun isFullscreen(): Boolean {
        return isFullscreenState
    }

    private fun setFullscreen(target: Boolean) {
        if (target == isFullscreenState) {
            return
        }

        val easyWindowState = easyWindowStateProvider()
        val composeWindow = easyWindowState?.composeWindow
        if (composeWindow == null) {
            easyWindowState?.state?.placement = if (target) {
                WindowPlacement.Fullscreen
            } else {
                WindowPlacement.Floating
            }
            isFullscreenState = target
            return
        }

        runOnEdtSync {
            when {
                isWindows -> {
                    if (target) {
                        enterWindowsBorderlessFullscreen(composeWindow)
                    } else {
                        exitWindowsBorderlessFullscreen(composeWindow)
                    }
                }

                else -> {
                    easyWindowState.state.placement = if (target) {
                        WindowPlacement.Fullscreen
                    } else {
                        WindowPlacement.Floating
                    }
                }
            }
            isFullscreenState = target
        }
    }

    private fun enterWindowsBorderlessFullscreen(window: ComposeWindow) {
        val current = windowsFullscreenSnapshot
        if (current?.window === window) {
            return
        }

        windowsFullscreenSnapshot = WindowsFullscreenSnapshot(
            window = window,
            bounds = Rectangle(window.bounds),
            extendedState = window.extendedState,
            isUndecorated = window.isUndecorated,
            isResizable = window.isResizable,
            isAlwaysOnTop = window.isAlwaysOnTop,
        )

        val screenBounds = window.graphicsConfiguration?.bounds
            ?: GraphicsEnvironment.getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .defaultConfiguration
                .bounds

        window.dispose()
        window.isUndecorated = true
        window.isResizable = false
        window.extendedState = Frame.NORMAL
        window.bounds = Rectangle(screenBounds)
        window.isAlwaysOnTop = true
        window.isVisible = true
        window.toFront()
    }

    private fun exitWindowsBorderlessFullscreen(window: ComposeWindow) {
        val snapshot = windowsFullscreenSnapshot ?: return
        windowsFullscreenSnapshot = null

        if (snapshot.window !== window) {
            return
        }

        window.dispose()
        window.isUndecorated = snapshot.isUndecorated
        window.isResizable = snapshot.isResizable
        window.bounds = Rectangle(snapshot.bounds)
        window.extendedState = snapshot.extendedState
        window.isAlwaysOnTop = snapshot.isAlwaysOnTop
        window.isVisible = true
        window.toFront()
    }

    private fun runOnEdtSync(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeAndWait(block)
        }
    }
}
