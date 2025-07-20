package com.easybangumi.next.shared.debug.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.easybangumi.next.shared.debug.DebugScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.jcef.JcefWebViewProxy
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.core.utils.WebViewHelperImpl



actual fun getWebView(): IWebView {
    return JcefWebViewProxy()
}