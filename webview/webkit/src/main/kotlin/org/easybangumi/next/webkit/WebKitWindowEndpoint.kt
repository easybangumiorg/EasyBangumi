package org.easybangumi.next.webkit

import android.view.View
import java.util.concurrent.atomic.AtomicReference


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

object WebKitWindowEndpoint {

    private data class Endpoint(
        val addBrowserToWindow: (View) -> Boolean,
        val removeBrowserFromWindow: (View) -> Unit,
    )

    private val endpointRef = AtomicReference<Endpoint?>(null)

    fun register(
        addBrowserToWindow: (View) -> Boolean,
        removeBrowserFromWindow: (View) -> Unit,
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

    fun addBrowserToWindow(webView: View): Boolean {
        return runCatching {
            endpointRef.get()?.addBrowserToWindow?.invoke(webView) ?: false
        }.getOrElse {
            false
        }
    }

    fun removeBrowserFromWindow(webView: View) {
        runCatching {
            endpointRef.get()?.removeBrowserFromWindow?.invoke(webView)
        }
    }

}