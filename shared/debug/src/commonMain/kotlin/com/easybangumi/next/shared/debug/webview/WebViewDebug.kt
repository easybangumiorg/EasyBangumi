package com.easybangumi.next.shared.debug.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.easybangumi.next.shared.debug.DebugScope
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.webview.IWebView

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

private val logger = logger("WebViewDebug")
@Composable
fun DebugScope.WebViewDebug() {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            scope.launch {
                test1()
            }
        }) {
            Text("test1")
        }
    }

}

private suspend fun test1() {
    val webview = getWebView()
    webview.loadUrl(
        "https://anime.girigirilove.com/playGV26627-2-1/",
    )
    webview.waitingForPageLoaded(5000L)
    val content = webview.getContent(500L)
    logger.info("WebView content: ${content}")
    webview.close()
}

expect fun getWebView(): IWebView