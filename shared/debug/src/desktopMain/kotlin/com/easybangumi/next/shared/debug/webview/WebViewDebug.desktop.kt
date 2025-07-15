package com.easybangumi.next.shared.debug.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.easybangumi.next.shared.debug.DebugScope
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewFactoryParam
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEFBrowser
import dev.datlag.kcef.KCEFClient
import kotlinx.coroutines.flow.MutableStateFlow
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRendering
import org.cef.callback.CefAuthCallback
import org.cef.callback.CefCallback
import org.cef.callback.CefDragData
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefRenderHandler
import org.cef.handler.CefRequestHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefScreenInfo
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.cef.security.CefSSLInfo
import org.easybangumi.next.kcef.KcefManager
import org.easybangumi.next.lib.logger.logger
import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.nio.ByteBuffer
import javax.swing.JPanel

private val logger = logger("WebViewDebug")

var kclient: KCEFClient? = null
var kbrowser: KCEFBrowser? = null
@Composable
actual fun DebugScope.WebViewDebug() {

    Box(modifier = Modifier.fillMaxSize()) {
        val uicomponent = remember {
            mutableStateOf<Component?>(null)
        }
        logger.info("WebViewDebug: ${uicomponent.value}")
        uicomponent.value?.let {
            SwingPanel(
                background = Color.Transparent,
                factory = {
                    it
                },
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(with(LocalDensity.current) {1080.toDp()})
            )
        }

        Button(onClick = {
//            val k = kbrowser
//            if (k != null) {
//                k.loadURL("https://anime.girigirilove.com/playGV26662-1-1/")
//                logger.info("Browser already exists: ${k.url}")
//                return@Button
//            }
            KcefManager.runWithKcefClient {
                it.addRequestHandler(object: CefRequestHandler {
                    override fun onBeforeBrowse(
                        p0: CefBrowser?,
                        p1: CefFrame?,
                        p2: CefRequest?,
                        p3: Boolean,
                        p4: Boolean
                    ): Boolean {
                        logger.info("onBeforeBrowse: ${p2?.url}")
                        return false // 返回 false 以允许浏览器继续处理请求
                    }

                    override fun onOpenURLFromTab(
                        p0: CefBrowser?,
                        p1: CefFrame?,
                        p2: String?,
                        p3: Boolean
                    ): Boolean {
                        logger.info("onOpenURLFromTab: $p2")
                        return false // 返回 false 以允许浏览器继续处理请求
                    }

                    override fun getResourceRequestHandler(
                        p0: CefBrowser?,
                        p1: CefFrame?,
                        p2:
                        CefRequest?,
                        p3: Boolean,
                        p4: Boolean,
                        p5: String?,
                        p6: BoolRef?
                    ): CefResourceRequestHandler? {
                        logger.info("getResourceRequestHandler: ${p2?.url}")
                        return null // 返回 null 以使用默认的资源请求处理器
                    }

                    override fun getAuthCredentials(
                        p0: CefBrowser?,
                        p1: String?,
                        p2: Boolean,
                        p3: String?,
                        p4: Int,
                        p5: String?,
                        p6: String?,
                        p7: CefAuthCallback?
                    ): Boolean {
                        logger.info("getAuthCredentials: $p1, $p2, $p3, $p4, $p5, $p6")
                        // 返回 true 以表示已处理认证请求
                        return true
                    }

                    override fun onCertificateError(
                        p0: CefBrowser?,
                        p1: CefLoadHandler.ErrorCode?,
                        p2: String?,
                        p3: CefSSLInfo?,
                        p4: CefCallback?
                    ): Boolean {
                        logger.info("onCertificateError: $p1, $p2")
                        // 返回 true 以表示已处理证书错误
                        return true
                    }

                    override fun onRenderProcessTerminated(
                        p0: CefBrowser?,
                        p1: CefRequestHandler.TerminationStatus?
                    ) {
                        logger.info("onRenderProcessTerminated: $p1")
                    }
                })
                val browser = it.createBrowser(
                    "https://anime.girigirilove.com/playGV26662-1-1/",
                    rendering = CefRendering.OFFSCREEN,
                )
            browser.wasResized(1080, 1080)
//        browser.loadURL("https://anime.girigirilove.com/playGV26662-1-1/")
                browser.loadURL("https://anime.girigirilove.com/playGV26662-1-1/")
                browser.setWindowVisibility(true)
                kbrowser = browser
                kclient = it

                browser.renderHandler.getViewRect(browser).setRect(0.0, 0.0, 1080.0, 1080.0)
//                uicomponent.value = browser.uiComponent
                browser.uiComponent?.size = Dimension(1080, 1080)
                browser.uiComponent?.isVisible = true
//                JPanel().add(browser.uiComponent)
////        browser.wasResized(1080, 1080)
                logger.info("Browser created: ${browser.url}")
            }


        }) {
            Text("test1")
        }
    }





}
