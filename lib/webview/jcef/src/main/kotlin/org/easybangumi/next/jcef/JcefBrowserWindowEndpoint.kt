package org.easybangumi.next.jcef

import java.awt.Component
import java.util.concurrent.atomic.AtomicReference

/**
 * Shared desktop window bridge for background JCEF browser hosting.
 */
object JcefBrowserWindowEndpoint {

    private data class Endpoint(
        val addBrowserToWindow: (Component) -> Boolean,
        val removeBrowserFromWindow: (Component) -> Unit,
    )

    private val endpointRef = AtomicReference<Endpoint?>(null)

    fun register(
        addBrowserToWindow: (Component) -> Boolean,
        removeBrowserFromWindow: (Component) -> Unit,
    ) {
        endpointRef.set(
            Endpoint(
                addBrowserToWindow = addBrowserToWindow,
                removeBrowserFromWindow = removeBrowserFromWindow,
            )
        )
    }

    fun unregister() {
        endpointRef.set(null)
    }

    fun addBrowserToWindow(browserComponent: Component): Boolean {
        return runCatching {
            endpointRef.get()?.addBrowserToWindow?.invoke(browserComponent) ?: false
        }.getOrElse {
            false
        }
    }

    fun removeBrowserFromWindow(browserComponent: Component) {
        runCatching {
            endpointRef.get()?.removeBrowserFromWindow?.invoke(browserComponent)
        }
    }
}
