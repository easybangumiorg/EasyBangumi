package com.heyanle.easybangumi4.plugin.source.utils.network

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.heyanle.easybangumi4.plugin.api.utils.api.JsRenderedResult
import com.heyanle.easybangumi4.plugin.api.utils.api.JsRenderedStrategy
import com.heyanle.easybangumi4.plugin.api.utils.api.JsVideoResult
import com.heyanle.easybangumi4.plugin.api.utils.api.JsVideoStrategy
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import com.heyanle.easybangumi4.plugin.source.utils.LightweightGettingWebViewClient
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.safeResume
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

class RenderHelperImpl(
    private val webViewHelperV2Impl: WebViewHelperV2Impl,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) : RenderHelper {

    companion object {
        private const val logTag = "RenderHelperImpl"

        private val videoUrlRegex = Regex(
            """(https?:)?//[^\s"'<>]+?\.(?:m3u8|mp4)(?:[^\s"'<>]*)?""",
            RegexOption.IGNORE_CASE,
        )

        private const val blobHookJs = """
        var origin = window.URL.createObjectURL
        window.URL.createObjectURL = function (t) {
            var blobUrl = origin(t)
            var xhr = new XMLHttpRequest()
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

            function notifyM3u8(url) {
                try {
                    if (url && String(url).indexOf("http") >= 0) {
                        window.videoParserBridge.onM3u8Url(String(url));
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

            function isM3u8Response(url, contentType, text) {
                try {
                    var normalizedUrl = String(url || "").toLowerCase();
                    var normalizedType = String(contentType || "").toLowerCase();
                    var normalizedText = String(text || "").replace(/^\s+/, "");
                    return normalizedText.indexOf("#EXTM3U") === 0 ||
                        normalizedType.indexOf("mpegurl") >= 0 ||
                        normalizedType.indexOf("vnd.apple.mpegurl") >= 0 ||
                        normalizedUrl.indexOf(".m3u8") >= 0;
                } catch (e) {
                    return false;
                }
            }

            function processVideoElement(video) {
                if (!video) return false;
                var src = video.getAttribute("src");
                if (src && src.trim() !== "" && !src.startsWith("blob:") && !isIgnored(src)) {
                    notify(src);
                    return true;
                }
                var sources = video.getElementsByTagName("source");
                for (var i = 0; i < sources.length; i++) {
                    src = sources[i].getAttribute("src");
                    if (src && src.trim() !== "" && !src.startsWith("blob:") && !isIgnored(src)) {
                        notify(src);
                        return true;
                    }
                }
                return false;
            }

            function scanVideoElements() {
                var videos = document.querySelectorAll("video");
                for (var i = 0; i < videos.length; i++) {
                    if (processVideoElement(videos[i])) return;
                }
            }

            function installVideoObserver() {
                scanVideoElements();
                var target = document.body || document.documentElement;
                if (!target) return;
                var observer = new MutationObserver(function (mutations) {
                    for (var m = 0; m < mutations.length; m++) {
                        var mutation = mutations[m];
                        if (mutation.type === "attributes" && mutation.target.nodeName === "VIDEO") {
                            if (processVideoElement(mutation.target)) {
                                observer.disconnect();
                                return;
                            }
                        }
                        for (var n = 0; n < mutation.addedNodes.length; n++) {
                            var node = mutation.addedNodes[n];
                            if (node.nodeName === "VIDEO" && processVideoElement(node)) {
                                observer.disconnect();
                                return;
                            }
                            if (node.querySelectorAll) {
                                var videos = node.querySelectorAll("video");
                                for (var i = 0; i < videos.length; i++) {
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

            function installIframeHooks() {
                var iframes = document.querySelectorAll("iframe");
                for (var i = 0; i < iframes.length; i++) {
                    iframes[i].addEventListener("load", scanVideoElements);
                }

                var target = document.body || document.documentElement;
                if (!target) return;
                var observer = new MutationObserver(function (mutations) {
                    for (var m = 0; m < mutations.length; m++) {
                        var mutation = mutations[m];
                        for (var n = 0; n < mutation.addedNodes.length; n++) {
                            var node = mutation.addedNodes[n];
                            if (node.nodeName === "IFRAME") {
                                node.addEventListener("load", scanVideoElements);
                            }
                            if (node.querySelectorAll) {
                                var nestedIframes = node.querySelectorAll("iframe");
                                for (var i = 0; i < nestedIframes.length; i++) {
                                    nestedIframes[i].addEventListener("load", scanVideoElements);
                                }
                            }
                        }
                    }
                });
                observer.observe(target, { childList: true, subtree: true });
            }

            function installM3u8FallbackHooks() {
                try {
                    if (window.__easybangumiM3u8FallbackInstalled) return;
                    window.__easybangumiM3u8FallbackInstalled = true;

                    var originFetch = window.fetch;
                    if (originFetch) {
                        window.fetch = function () {
                            var args = arguments;
                            var requestUrl = args && args[0] && (args[0].url || args[0]);
                            return originFetch.apply(this, args).then(function (response) {
                                try {
                                    var responseUrl = response.url || requestUrl;
                                    var type = response.headers && response.headers.get && response.headers.get("content-type");
                                    if (isM3u8Response(responseUrl, type, "")) {
                                        notifyM3u8(responseUrl);
                                        return response;
                                    }
                                    var cloned = response.clone && response.clone();
                                    if (cloned && cloned.text) {
                                        cloned.text().then(function (text) {
                                            try {
                                                if (isM3u8Response(responseUrl, type, text)) {
                                                    notifyM3u8(responseUrl);
                                                }
                                            } catch (e) {}
                                        }).catch(function () {});
                                    }
                                } catch (e) {}
                                return response;
                            });
                        };
                    }

                    var xhrOpen = window.XMLHttpRequest && window.XMLHttpRequest.prototype.open;
                    if (xhrOpen) {
                        window.XMLHttpRequest.prototype.open = function () {
                            var args = arguments;
                            var requestUrl = args && args[1];
                            this.addEventListener("load", function () {
                                try {
                                    var text = this.responseText;
                                    var type = this.getResponseHeader && this.getResponseHeader("content-type");
                                    if (isM3u8Response(this.responseURL || requestUrl, type, text)) {
                                        notifyM3u8(this.responseURL || requestUrl);
                                    }
                                } catch (e) {}
                            });
                            return xhrOpen.apply(this, args);
                        };
                    }
                } catch (e) {}
            }

            if (document.readyState === "loading") {
                document.addEventListener("DOMContentLoaded", function () {
                    installVideoObserver();
                    installIframeHooks();
                    installM3u8FallbackHooks();
                });
            } else {
                installVideoObserver();
                installIframeHooks();
                installM3u8FallbackHooks();
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
                var src = iframe && iframe.getAttribute("src");
                if (src) notify(src);
            }

            function scanIframes() {
                var iframes = document.querySelectorAll("iframe");
                for (var i = 0; i < iframes.length; i++) {
                    processIframeElement(iframes[i]);
                }
            }

            var observer = new MutationObserver(function (mutations) {
                for (var m = 0; m < mutations.length; m++) {
                    var mutation = mutations[m];
                    if (mutation.type === "attributes" && mutation.target.nodeName === "IFRAME") {
                        processIframeElement(mutation.target);
                    } else {
                        for (var n = 0; n < mutation.addedNodes.length; n++) {
                            var node = mutation.addedNodes[n];
                            if (node.nodeName === "IFRAME") processIframeElement(node);
                            if (node.querySelectorAll) {
                                var iframes = node.querySelectorAll("iframe");
                                for (var i = 0; i < iframes.length; i++) {
                                    processIframeElement(iframes[i]);
                                }
                            }
                        }
                    }
                }
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

    override fun renderHtmlFromJs(strategy: JsRenderedStrategy): JsRenderedResult {
        val renderedStrategy = strategy.toRenderedStrategy()
        var res: RenderHelper.RenderedResult? = null
        val countDownLatch = CountDownLatch(1)
        scope.launch {
            res = renderedHtml(renderedStrategy)
            countDownLatch.countDown()
        }
        countDownLatch.await(10, TimeUnit.SECONDS)
        return (res ?: RenderHelper.RenderedResult(
            strategy = renderedStrategy,
            url = "",
            isTimeout = true,
            content = "",
            interceptResource = ""
        )).toJsRenderedResult()
    }

    override fun renderVideoFromJs(strategy: JsVideoStrategy): JsVideoResult {
        val videoStrategy = strategy.toVideoStrategy()
        var res: RenderHelper.VideoResult? = null
        val countDownLatch = CountDownLatch(1)
        scope.launch {
            res = renderVideo(videoStrategy)
            countDownLatch.countDown()
        }
        countDownLatch.await(videoStrategy.timeOut + 1000L, TimeUnit.MILLISECONDS)
        return (res ?: RenderHelper.VideoResult(
            strategy = videoStrategy,
            url = "",
            isTimeout = true,
            isM3u8 = false
        )).toJsVideoResult()
    }

    private fun JsRenderedStrategy.toRenderedStrategy(): RenderHelper.RenderedStrategy {
        return RenderHelper.RenderedStrategy(
            url = url,
            callBackRegex = callBackRegex,
            encoding = encoding,
            userAgentString = userAgentString,
            header = header,
            actionJs = actionJs,
            isBlockBlob = isBlockBlob,
            timeOut = timeOut,
            isBlockResource = isBlockResource,
        )
    }

    private fun RenderHelper.RenderedResult.toJsRenderedResult(): JsRenderedResult {
        return JsRenderedResult(
            strategy.toJsRenderedStrategy(),
            url,
            isTimeout,
            content,
            interceptResource,
        )
    }

    private fun RenderHelper.RenderedStrategy.toJsRenderedStrategy(): JsRenderedStrategy {
        return JsRenderedStrategy(
            url,
            callBackRegex,
            encoding,
            userAgentString,
            header,
            actionJs,
            isBlockBlob,
            timeOut,
            isBlockResource,
        )
    }

    private fun JsVideoStrategy.toVideoStrategy(): RenderHelper.VideoStrategy {
        return RenderHelper.VideoStrategy(
            url = url,
            userAgentString = userAgentString,
            header = header,
            actionJs = actionJs,
            timeOut = timeOut,
            useLegacyParser = useLegacyParser,
        )
    }

    private fun RenderHelper.VideoResult.toJsVideoResult(): JsVideoResult {
        return JsVideoResult(
            strategy.toJsVideoStrategy(),
            url,
            isTimeout,
            isM3u8,
        )
    }

    private fun RenderHelper.VideoStrategy.toJsVideoStrategy(): JsVideoStrategy {
        return JsVideoStrategy(
            url,
            userAgentString,
            header,
            actionJs,
            timeOut,
            useLegacyParser,
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

            var fallbackVideoUrl: String? = null
            var resultFromM3u8Hook = false
            val result = withTimeoutOrNull(strategy.timeOut) {
                suspendCancellableCoroutine<String> { continuation ->
                    var completed = false
                    val probingUrls = mutableSetOf<String>()

                    fun complete(url: String?, isM3u8Hook: Boolean = false) {
                        val normalized = normalizeVideoUrl(url, strategy.url) ?: return
                        if (isAdUrl(normalized) || completed) return
                        resultFromM3u8Hook = isM3u8Hook || isM3u8Url(normalized)
                        debugLog("renderVideo complete page=${strategy.url} url=$normalized legacy=${strategy.useLegacyParser} m3u8=$resultFromM3u8Hook")
                        completed = true
                        continuation.safeResume(normalized)
                    }

                    fun probeAndComplete(url: String?) {
                        val normalized = normalizeVideoUrl(url, strategy.url) ?: return
                        if (!probingUrls.add(normalized) || completed) return
                        scope.launch(Dispatchers.IO) {
                            val probe = probeVideoUrl(normalized, strategy)
                            if (probe != null) {
                                complete(probe.url, isM3u8Hook = probe.isM3u8)
                            }
                        }
                    }

                    webview.addJavascriptInterface(object : Any() {
                        @JavascriptInterface
                        fun onVideoUrl(url: String?) {
                            webview.post {
                                complete(url)
                            }
                        }

                        @JavascriptInterface
                        fun onM3u8Url(url: String?) {
                            webview.post {
                                complete(url, isM3u8Hook = true)
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
                                debugLog("renderVideo intercept candidate page=${strategy.url} url=$url")
                                fallbackVideoUrl = normalizeVideoUrl(url, strategy.url)
                                if (isM3u8Url(url)) {
                                    complete(url, isM3u8Hook = true)
                                } else {
                                    probeAndComplete(url)
                                }
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
                        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                            if (!strategy.useLegacyParser && isM3u8Url(url)) {
                                debugLog("renderVideo intercept m3u8 page=${strategy.url} url=$url")
                                complete(url, isM3u8Hook = true)
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

            val fallbackProbe = if (result == null) {
                withContext(Dispatchers.IO) { probeVideoUrl(fallbackVideoUrl, strategy) }
            } else {
                null
            }
            val finalUrl = result ?: fallbackProbe?.url.orEmpty()
            RenderHelper.VideoResult(
                strategy = strategy,
                url = finalUrl,
                isTimeout = finalUrl.isBlank(),
                isM3u8 = resultFromM3u8Hook || fallbackProbe?.isM3u8 == true || isM3u8Url(finalUrl),
            ).also {
                debugLog("renderVideo finished page=${strategy.url} timeout=${it.isTimeout} m3u8=${it.isM3u8} url=${it.url}")
            }
        }
    }

    private fun debugLog(message: String) {
        runCatching { message.logi(logTag) }
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

    private fun probeVideoUrl(url: String?, strategy: RenderHelper.VideoStrategy): VideoProbe? {
        val normalized = normalizeVideoUrl(url, strategy.url) ?: return null
        if (isAdUrl(normalized)) return null
        val request = Request.Builder()
            .url(normalized)
            .header("Range", "bytes=0-2047")
            .apply {
                strategy.userAgentString?.takeIf { it.isNotBlank() }?.let { header("User-Agent", it) }
                strategy.header.orEmpty().forEach { (key, value) -> header(key, value) }
            }
            .build()
        return runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) return null
                val contentType = response.header("Content-Type").orEmpty().lowercase()
                val bodyPrefix = response.body?.string().orEmpty().trimStart()
                when {
                    bodyPrefix.startsWith("#EXTM3U") ||
                        contentType.contains("mpegurl") ||
                        contentType.contains("vnd.apple.mpegurl") -> {
                        debugLog("renderVideo probe m3u8 url=$normalized contentType=$contentType")
                        VideoProbe(normalized, isM3u8 = true)
                    }
                    contentType.startsWith("video/") || contentType.contains("octet-stream") -> {
                        debugLog("renderVideo probe video url=$normalized contentType=$contentType")
                        VideoProbe(normalized, isM3u8 = false)
                    }
                    else -> null
                }
            }
        }.getOrNull()
    }

    private data class VideoProbe(
        val url: String,
        val isM3u8: Boolean,
    )
}
