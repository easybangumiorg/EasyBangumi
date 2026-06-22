package com.heyanle.easybangumi4.plugin.source.utils.network

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import com.heyanle.easybangumi4.plugin.source.utils.LightweightGettingWebViewClient
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.safeResume
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

class RenderHelperImpl(
    private val webViewHelperV2Impl: WebViewHelperV2Impl,
) : RenderHelper {

    companion object {
        private val videoUrlRegex = Regex(
            """(https?:)?//[^\s"'<>]+?\.(?:m3u8|mp4)(?:[^\s"'<>]*)?""",
            RegexOption.IGNORE_CASE,
        )

        private const val blobHookJs = """
        let origin = window.URL.createObjectURL
        window.URL.createObjectURL = function (t) {
            let blobUrl = origin(t)
            let xhr = new XMLHttpRequest()
            xhr.onload = function () {
                 window.blobHook.handleWrapper(xhr.responseText)
            }
            xhr.open('get', blobUrl)
            xhr.send();
            return blobUrl
        }
    """

        private const val videoParserBridgeName = "videoParserBridge"

        private const val videoParserJs = """
        (function () {
            if (window.__easybangumiVideoParserInstalled) {
                return;
            }
            window.__easybangumiVideoParserInstalled = true;

            function notify(url) {
                try {
                    if (url && String(url).indexOf("http") >= 0) {
                        window.videoParserBridge.onVideoUrl(String(url));
                    }
                } catch (e) {}
            }

            function isIgnored(url) {
                return !url ||
                    String(url).indexOf("googleads") >= 0 ||
                    String(url).indexOf("googlesyndication.com") >= 0 ||
                    String(url).indexOf("prestrain.html") >= 0 ||
                    String(url).indexOf("prestrain%2Ehtml") >= 0 ||
                    String(url).indexOf("adtrafficquality") >= 0 ||
                    String(url).indexOf("doubleclick") >= 0;
            }

            function processVideoElement(video) {
                if (!video) return false;
                let src = video.getAttribute("src");
                if (src && src.trim() !== "" && !src.startsWith("blob:") && !isIgnored(src)) {
                    notify(src);
                    return true;
                }
                const sources = video.getElementsByTagName("source");
                for (let i = 0; i < sources.length; i++) {
                    src = sources[i].getAttribute("src");
                    if (src && src.trim() !== "" && !src.startsWith("blob:") && !isIgnored(src)) {
                        notify(src);
                        return true;
                    }
                }
                return false;
            }

            function scanVideoElements() {
                const videos = document.querySelectorAll("video");
                for (let i = 0; i < videos.length; i++) {
                    if (processVideoElement(videos[i])) return;
                }
            }

            function installVideoObserver() {
                scanVideoElements();
                const target = document.body || document.documentElement;
                if (!target) return;
                const observer = new MutationObserver((mutations) => {
                    for (const mutation of mutations) {
                        if (mutation.type === "attributes" && mutation.target.nodeName === "VIDEO") {
                            if (processVideoElement(mutation.target)) {
                                observer.disconnect();
                                return;
                            }
                        }
                        for (const node of mutation.addedNodes) {
                            if (node.nodeName === "VIDEO" && processVideoElement(node)) {
                                observer.disconnect();
                                return;
                            }
                            if (node.querySelectorAll) {
                                const videos = node.querySelectorAll("video");
                                for (let i = 0; i < videos.length; i++) {
                                    if (processVideoElement(videos[i])) {
                                        observer.disconnect();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                });
                observer.observe(target, {
                    childList: true,
                    subtree: true,
                    attributes: true,
                    attributeFilter: ["src"]
                });
            }

            function installM3u8Hooks(scopeWindow) {
                try {
                    if (scopeWindow.__easybangumiM3u8HookInstalled) return;
                    scopeWindow.__easybangumiM3u8HookInstalled = true;

                    const responseText = scopeWindow.Response && scopeWindow.Response.prototype.text;
                    if (responseText) {
                        scopeWindow.Response.prototype.text = function () {
                            return new Promise((resolve, reject) => {
                                responseText.call(this).then((text) => {
                                    resolve(text);
                                    try {
                                        if (text && text.trim().startsWith("#EXTM3U")) {
                                            notify(this.url);
                                        }
                                    } catch (e) {}
                                }).catch(reject);
                            });
                        };
                    }

                    const xhrOpen = scopeWindow.XMLHttpRequest && scopeWindow.XMLHttpRequest.prototype.open;
                    if (xhrOpen) {
                        scopeWindow.XMLHttpRequest.prototype.open = function (...args) {
                            this.addEventListener("load", () => {
                                try {
                                    const content = this.responseText;
                                    if (content && content.trim().startsWith("#EXTM3U")) {
                                        notify(args[1]);
                                    }
                                } catch (e) {}
                            });
                            return xhrOpen.apply(this, args);
                        };
                    }
                } catch (e) {}
            }

            function installIframeHooks() {
                function injectIntoIframe(iframe) {
                    try {
                        const iframeWindow = iframe.contentWindow;
                        if (iframeWindow) {
                            installM3u8Hooks(iframeWindow);
                        }
                    } catch (e) {}
                }

                document.querySelectorAll("iframe").forEach((iframe) => {
                    injectIntoIframe(iframe);
                    iframe.addEventListener("load", () => injectIntoIframe(iframe));
                });

                const target = document.body || document.documentElement;
                if (!target) return;
                const observer = new MutationObserver((mutations) => {
                    for (const mutation of mutations) {
                        for (const node of mutation.addedNodes) {
                            if (node.nodeName === "IFRAME") {
                                node.addEventListener("load", () => injectIntoIframe(node));
                            }
                            if (node.querySelectorAll) {
                                node.querySelectorAll("iframe").forEach((iframe) => {
                                    iframe.addEventListener("load", () => injectIntoIframe(iframe));
                                });
                            }
                        }
                    }
                });
                observer.observe(target, { childList: true, subtree: true });
            }

            installM3u8Hooks(window);
            if (document.readyState === "loading") {
                document.addEventListener("DOMContentLoaded", () => {
                    installVideoObserver();
                    installIframeHooks();
                });
            } else {
                installVideoObserver();
                installIframeHooks();
            }
        })();
        """

        private const val legacyVideoParserJs = """
        (function () {
            if (window.__easybangumiLegacyVideoParserInstalled) {
                return;
            }
            window.__easybangumiLegacyVideoParserInstalled = true;

            function notify(url) {
                try {
                    if (url && (String(url).indexOf("http") >= 0 || String(url).startsWith("//"))) {
                        window.videoParserBridge.onLegacyUrl(String(url));
                    }
                } catch (e) {}
            }

            function processIframeElement(iframe) {
                const src = iframe && iframe.getAttribute("src");
                if (src) notify(src);
            }

            function scanIframes() {
                document.querySelectorAll("iframe").forEach(processIframeElement);
            }

            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.type === "attributes" && mutation.target.nodeName === "IFRAME") {
                        processIframeElement(mutation.target);
                    } else {
                        mutation.addedNodes.forEach((node) => {
                            if (node.nodeName === "IFRAME") processIframeElement(node);
                            if (node.querySelectorAll) {
                                node.querySelectorAll("iframe").forEach(processIframeElement);
                            }
                        });
                    }
                });
            });

            observer.observe(document.documentElement, {
                childList: true,
                subtree: true,
                attributes: true,
                attributeFilter: ["src"]
            });
            scanIframes();
        })();
        """

        private const val videoElementPollingJs = """
        (function () {
            var videos = document.querySelectorAll("video");
            for (var i = 0; i < videos.length; i++) {
                var src = videos[i].getAttribute("src");
                if (src && src.trim() !== "" && !src.startsWith("blob:") && src.indexOf("googleads") < 0) {
                    window.videoParserBridge.onVideoUrl(src);
                    return;
                }
                var sources = videos[i].getElementsByTagName("source");
                for (var j = 0; j < sources.length; j++) {
                    src = sources[j].getAttribute("src");
                    if (src && src.trim() !== "" && !src.startsWith("blob:") && src.indexOf("googleads") < 0) {
                        window.videoParserBridge.onVideoUrl(src);
                        return;
                    }
                }
            }
        })();
        """

        private const val legacyIframePollingJs = """
        (function () {
            var iframes = document.querySelectorAll("iframe");
            for (var i = 0; i < iframes.length; i++) {
                var src = iframes[i].getAttribute("src");
                if (src) {
                    window.videoParserBridge.onLegacyUrl(src);
                }
            }
        })();
        """
    }

    private val scope = MainScope()

    fun renderHtmlFromJs(strategy: RenderHelper.RenderedStrategy): RenderHelper.RenderedResult {
        var res: RenderHelper.RenderedResult? = null
        val countDownLatch = CountDownLatch(1)
        scope.launch {
            res = renderedHtml(strategy)
            countDownLatch.countDown()
        }
        countDownLatch.await(10, TimeUnit.SECONDS)
        return res ?: RenderHelper.RenderedResult(
            strategy = strategy,
            url = "",
            isTimeout = true,
            content = "",
            interceptResource = ""
        )
    }

    fun renderVideoFromJs(strategy: RenderHelper.VideoStrategy): RenderHelper.VideoResult {
        var res: RenderHelper.VideoResult? = null
        val countDownLatch = CountDownLatch(1)
        scope.launch {
            res = renderVideo(strategy)
            countDownLatch.countDown()
        }
        countDownLatch.await(strategy.timeOut + 1000L, TimeUnit.MILLISECONDS)
        return res ?: RenderHelper.VideoResult(
            strategy = strategy,
            url = "",
            isTimeout = true,
        )
    }

    override suspend fun renderedHtml(strategy: RenderHelper.RenderedStrategy): RenderHelper.RenderedResult {
        val webview = webViewHelperV2Impl.getGlobalWebViewOrNull() ?: throw WebViewCreatedException()
        return withContext(Dispatchers.Main) {
            webview.clearWeb()
            webview.settings.apply {
                setUserAgentString(strategy.userAgentString ?: userAgentString)
                defaultTextEncodingName = strategy.encoding
            }
            webview.resumeTimers()

            if (!strategy.isBlockBlob) {
                webview.loadUrl(strategy.url, strategy.header.orEmpty())
                var r = webview.waitUntil(
                    if (strategy.callBackRegex.isEmpty()) null else Regex(strategy.callBackRegex),
                    strategy.timeOut,
                    true,
                    ignoreTimeoutExt = true
                )
                if (r.isNotEmpty() || strategy.actionJs == null) {
                    val content = webview.getHtml().also {
                        webview.stop()
                        webViewHelperV2Impl.recyclerWebView(webview)
                    }
                    return@withContext RenderHelper.RenderedResult(
                        strategy,
                        strategy.url,
                        false,
                        content,
                        r
                    )
                }
                webview.evaluateJavascript(strategy.actionJs)
                r = try {
                    webview.waitUntil(
                        if (strategy.callBackRegex.isEmpty()) null else Regex(strategy.callBackRegex),
                        strategy.timeOut,
                        true,
                        ignoreTimeoutExt = false
                    )
                } catch (e: CancellationException) {
                    e.printStackTrace()
                    webViewHelperV2Impl.recyclerWebView(webview)
                    return@withContext RenderHelper.RenderedResult(
                        strategy,
                        strategy.url,
                        true,
                        "",
                        ""
                    )
                }
                val content = webview.getHtml().also {
                    webview.stop()
                    webViewHelperV2Impl.recyclerWebView(webview)
                }
                return@withContext RenderHelper.RenderedResult(
                    strategy,
                    strategy.url,
                    false,
                    content,
                    r
                )
            } else {
                val targetRegex = Regex(strategy.callBackRegex)
                webview.webViewClient =
                    object : LightweightGettingWebViewClient(targetRegex, false) {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            webview.evaluateJavascript(blobHookJs)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            webview.evaluateJavascript(strategy.actionJs)
                        }
                    }
                val blobResource = withTimeoutOrNull(strategy.timeOut) {
                    suspendCoroutine { con ->
                        webview.addJavascriptInterface(object : Any() {
                            @JavascriptInterface
                            fun handleWrapper(blobTextData: String) {
                                if (targetRegex.containsMatchIn(blobTextData)) {
                                    webview.removeJavascriptInterface("blobHook")
                                    con.resume(blobTextData)
                                }
                            }
                        }, "blobHook")
                        webview.loadUrl(strategy.url, strategy.header.orEmpty())
                    }
                }
                if (blobResource == null) {
                    webview.stop()
                    webViewHelperV2Impl.recyclerWebView(webview)
                    return@withContext RenderHelper.RenderedResult(
                        strategy,
                        strategy.url,
                        true,
                        "",
                        ""
                    )
                } else {
                    val content = webview.getHtml().also {
                        webview.stop()
                        webViewHelperV2Impl.recyclerWebView(webview)
                    }
                    return@withContext RenderHelper.RenderedResult(
                        strategy,
                        strategy.url,
                        false,
                        content,
                        blobResource
                    )
                }
            }
        }
    }

    override suspend fun renderVideo(strategy: RenderHelper.VideoStrategy): RenderHelper.VideoResult {
        val webview = webViewHelperV2Impl.getGlobalWebViewOrNull() ?: throw WebViewCreatedException()
        return withContext(Dispatchers.Main) {
            webview.clearWeb()
            webview.settings.apply {
                setUserAgentString(strategy.userAgentString ?: userAgentString)
                mediaPlaybackRequiresUserGesture = false
                loadsImagesAutomatically = false
                blockNetworkImage = true
            }
            webview.resumeTimers()

            val result = withTimeoutOrNull(strategy.timeOut) {
                suspendCancellableCoroutine<String> { continuation ->
                    var completed = false

                    fun complete(url: String?) {
                        val normalized = normalizeVideoUrl(url, strategy.url) ?: return
                        if (isAdUrl(normalized) || completed) return
                        completed = true
                        continuation.safeResume(normalized)
                    }

                    webview.addJavascriptInterface(object : Any() {
                        @JavascriptInterface
                        fun onVideoUrl(url: String?) {
                            webview.post {
                                complete(url)
                            }
                        }

                        @JavascriptInterface
                        fun onLegacyUrl(url: String?) {
                            webview.post {
                                complete(decodeVideoSource(url))
                            }
                        }
                    }, videoParserBridgeName)

                    webview.webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            webview.evaluateJavascript(if (strategy.useLegacyParser) legacyVideoParserJs else videoParserJs)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            webview.evaluateJavascript(if (strategy.useLegacyParser) legacyVideoParserJs else videoParserJs)
                            webview.evaluateJavascript(strategy.actionJs)
                            scope.launch {
                                while (!completed && continuation.isActive) {
                                    webview.evaluateJavascript(
                                        if (strategy.useLegacyParser) legacyIframePollingJs else videoElementPollingJs
                                    )
                                    kotlinx.coroutines.delay(1000L)
                                }
                            }
                        }

                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url?.toString()
                            if (!strategy.useLegacyParser && isVideoRequest(url, request?.requestHeaders)) {
                                webview.post {
                                    complete(url)
                                }
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
                        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                            if (!strategy.useLegacyParser && isM3u8Url(url)) {
                                webview.post {
                                    complete(url)
                                }
                            }
                            return super.shouldInterceptRequest(view, url)
                        }
                    }

                    continuation.invokeOnCancellation {
                        webview.removeJavascriptInterface(videoParserBridgeName)
                    }

                    webview.loadUrl(strategy.url, strategy.header.orEmpty())
                }
            }

            webview.removeJavascriptInterface(videoParserBridgeName)
            webview.stop()
            webViewHelperV2Impl.recyclerWebView(webview)

            RenderHelper.VideoResult(
                strategy = strategy,
                url = result.orEmpty(),
                isTimeout = result == null,
            )
        }
    }

    private fun normalizeVideoUrl(url: String?, baseUrl: String): String? {
        val raw = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val candidate = when {
            raw.startsWith("//") -> "https:$raw"
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.startsWith("/") -> runCatching {
                val base = URI(baseUrl)
                URI(base.scheme, base.authority, raw, null, null).toString()
            }.getOrNull() ?: raw
            else -> raw
        }
        if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
            return candidate
        }
        return null
    }

    private fun decodeVideoSource(url: String?): String? {
        val normalized = normalizeVideoUrl(url, "") ?: return null
        val decoded = runCatching {
            java.net.URLDecoder.decode(normalized, "UTF-8")
        }.getOrDefault(normalized)

        if (isDirectVideoUrl(normalized)) {
            return normalized
        }

        runCatching {
            val uri = URI(decoded)
            uri.rawQuery?.split("&").orEmpty()
                .mapNotNull { part -> part.substringAfter("=", missingDelimiterValue = "").takeIf { it.isNotEmpty() } }
                .forEach { value ->
                    val queryValue = java.net.URLDecoder.decode(value, "UTF-8")
                    val match = videoUrlRegex.find(queryValue)?.value
                    if (match != null) {
                        return normalizeVideoUrl(match, decoded)
                    }
                }
        }

        return videoUrlRegex.find(decoded)?.value?.let { normalizeVideoUrl(it, decoded) }
    }

    private fun isDirectVideoUrl(url: String): Boolean {
        val lower = url.lowercase()
        return !isAdUrl(lower) && runCatching {
            val path = URI(lower).path
            path.endsWith(".m3u8") || path.endsWith(".mp4")
        }.getOrDefault(lower.contains(".m3u8") || lower.contains(".mp4"))
    }

    private fun isM3u8Url(url: String?): Boolean {
        val lower = url?.lowercase() ?: return false
        return !isAdUrl(lower) && runCatching {
            URI(lower).path.endsWith(".m3u8")
        }.getOrDefault(lower.contains(".m3u8"))
    }

    private fun isVideoRequest(url: String?, headers: Map<String, String>?): Boolean {
        val lower = url?.lowercase() ?: return false
        if (isAdUrl(lower)) return false
        if (isM3u8Url(lower)) return true

        val range = headers?.get("Range") ?: headers?.get("range")
        if (range?.startsWith("bytes=") != true) return false
        val excluded = listOf(".js", ".css", ".html", ".json", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".woff", ".woff2", ".wasm")
        return excluded.none { lower.substringBefore("?").endsWith(it) }
    }

    private fun isAdUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("googleads") ||
            lower.contains("googlesyndication") ||
            lower.contains("adtrafficquality") ||
            lower.contains("doubleclick")
    }
}
