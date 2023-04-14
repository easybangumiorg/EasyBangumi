package com.heyanle.lib_anim.utils.network.webview_helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import com.heyanle.lib_anim.utils.network.webview_helper.WebViewHelperImpl.BlobIntercept
import com.heyanle.lib_anim.utils.setDefaultSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by HeYanLe on 2023/2/3 22:39.
 * https://github.com/heyanLE
 */
class WebViewHelperImpl(
    private val context: Context
) : WebviewHelper {

    val cookieManager = CookieManager.getInstance()

    companion object {
        private const val blobHook = """
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
    }


    private var blobIntercept: BlobIntercept? = null

    val globalWebView by lazy(LazyThreadSafetyMode.NONE) {
        WebView(context.applicationContext).apply {
            setDefaultSettings()
            cookieManager.also {
                it.setAcceptCookie(true)
                it.acceptCookie()
                // 跨域cookie读取
                it.setAcceptThirdPartyCookies(this, true)
            }
            //BlobHook回调
            addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun handleWrapper(blobTextData: String) {
                    if (blobIntercept?.handle(blobTextData) == true) {
                        //每次handle一次性
                        blobIntercept = null
                    }
                }
            }, "blobHook")
        }
    }

    private val cb = ValueCallback<Boolean> { }
    fun WebView.clearWeb() {
        clearHistory()
        clearFormData()
        clearMatches()
    }

    fun WebView.executeJavaScriptCode(code: String) {
        loadUrl("javascript:(function(){$code})()")
    }

    /**
     * @param blockRes 屏蔽资源，加快抓取速度
     */
    private abstract class LightweightGettingWebViewClient(
        private val targetRegex: Regex,
        private val blockReqForward: Boolean = true,
        private val blockRes: Array<String> = arrayOf(
            ".css",
            ".mp4", ".ts",
            ".mp3", ".m4a",
            ".gif", ",jpg", ".png", ".webp"
        )
    ) : WebViewClientCompat() {

        private val blockWebResourceRequest =
            WebResourceResponse("text/html", "utf-8", ByteArrayInputStream("".toByteArray()))

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.proceed()
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceErrorCompat
        ) {

            super.onReceivedError(view, request, error)
        }


        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return false
        }


//        /**
//         * 拦截无关资源文件
//         *
//         * 注意，该方法运行在线程池内
//         */
//        @SuppressLint("RequiresFeature")
//        @CallSuper
//        override fun shouldInterceptRequest(
//            view: WebView,
//            request: WebResourceRequest?
//        ) = run {
//            val url = request?.url?.toString()
//                ?: return@run super.shouldInterceptRequest(view, request)
//            //阻止无关资源加载，加快获取速度
//            if (!targetRegex.matches(url) && url.containStrs(*blockRes)) {
//                //Log.d("禁止加载", url)
//                //转发回onLoadResource
//                if (blockReqForward)
//                    view.post { WebViewCompat.getWebViewClient(view).onLoadResource(view, url) }
//                blockWebResourceRequest
//            } else
//                super.shouldInterceptRequest(view, request)
//        }
    }

    override suspend fun getRenderedHtmlCode(
        url: String,
        callBackRegex: String,
        encoding: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        return withContext(Dispatchers.Main) {

            // globalWebView.clearWeb()

            suspendCoroutine { con ->
                Log.d("getRenderedHtmlStart", url)
                val regexE = Regex(callBackRegex)
                var hasResult = false

                fun callBack(web: WebView) {
                    hasResult = true

                    web.executeJavaScriptCode(actionJs ?: "")
                    web.evaluateJavascript("(function() { return document.documentElement.outerHTML })()") {
                        Log.d("WebviewHelper", "script back" + url)
                        if (it.isNullOrEmpty())
                            con.resume("")
                        else {
                            launch(Dispatchers.Default) {
                                val source = StringEscapeUtils.unescapeEcmaScript(it)
                                Log.d("get code completely", source)
                                con.resume(source)
                                withContext(Dispatchers.Main) {
                                    web.apply {
                                        stopLoading()
                                        pauseTimers()
                                    }
                                }
                            }
                        }
                    }
                }

                globalWebView.settings.apply {
                    setUserAgentString(userAgentString)
                    defaultTextEncodingName = encoding
                }
                globalWebView.webViewClient = object : LightweightGettingWebViewClient(regexE) {

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                    }

                    //由于ajax存在可能不是真正完全加载
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)


                        Log.d("WebviewHelper", ("onPageFinished url ->$url"))
                        if (!hasResult && callBackRegex.isBlank())
                            callBack(view)
                    }

                    override fun onLoadResource(view: WebView, url: String) {
                        Log.d("WebviewHelper", " 1.url -> " + url)
                        if (callBackRegex.isNotBlank() && !hasResult && regexE.matches(url)) {
                            Log.d("url matched", url)
                            callBack(view)
                        }
                        super.onLoadResource(view, url)
                    }

//                    override fun shouldInterceptRequest(
//                        view: WebView?,
//                        request: WebResourceRequest?
//                    ): WebResourceResponse? {
//                        Log.d("WebUtilImpl", request?.url.toString() + " -> " + request?.requestHeaders?.get("Cookie"))
//
//
////                        request?.let {
////                            val uurl = if(url.endsWith("/")) url else "${url}/"
////                            val udd = (it.url?.toString()?:"")
////                            Log.d("WebViewHelperImpl", "$uurl -- $udd ${uurl == udd}")
////                            it.requestHeaders.remove("X-Requested-With")
////                            if(it.method == "GET" && uurl == udd){
////
////
////
////                                it.requestHeaders.remove("X-Requested-With")
////
////                                val req = GET(it.url.toString(), Headers.Builder().apply {
////                                    it.requestHeaders.iterator().forEach {
////                                        add(it.key, it.value)
////                                    }
////                                }.build(), CacheControl.Builder().noCache().build())
////                                val resp = networkHelper.client.newCall(req).execute()
////                                val contentType = resp.headers.get("Content-Type")
////
////                                val mimeType = contentType?.split("; charset=")?.get(0)?:"text/html"
////                                val encoding = contentType?.split("; charset=")?.get(1)?:"utf-8"
////                                val webResourceResponse = WebResourceResponse(mimeType, encoding, ByteArrayInputStream(resp?.body?.string()?.toByteArray()));
////                                return webResourceResponse
////
////                            }
////
////                        }
//                        val resp =  super.shouldInterceptRequest(view, request)
//                        Log.d("WebUtilImpl", request?.url.toString() + " <- " + resp?.responseHeaders?.get("Set-Cookie"))
//                        return resp
//                    }
                }
                globalWebView.resumeTimers()
                globalWebView.loadUrl(url, header ?: emptyMap())
                launch(Dispatchers.Main) {
                    delay(timeOut)
                    if (!hasResult)
                        callBack(globalWebView)
                }
            }
        }
    }


    override suspend fun interceptResource(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String =
        withContext(Dispatchers.Main) {
            Log.d("WebViewHelper", "interceptResource start regex->$regex")

            // globalWebView.clearWeb()

            var hasResult = false
            val regexE = Regex(regex)
            suspendCoroutine { con ->
                globalWebView.settings.userAgentString = userAgentString
                globalWebView.webViewClient = object : LightweightGettingWebViewClient(regexE) {

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        actionJs?.let { view?.executeJavaScriptCode(it) }
                    }

                    override fun onLoadResource(view: WebView?, url: String) {
                        Log.d("WebViewHelper", "url->" + url)
                        if (!hasResult && regexE.matches(url)) {
                            con.resume(url)
                            hasResult = true
                            view?.stopLoading()
                            view?.pauseTimers()
                        }
                        super.onLoadResource(view, url)
                    }
                }
                globalWebView.resumeTimers()
                globalWebView.loadUrl(url, header ?: emptyMap())
                launch(Dispatchers.Main) {
                    delay(timeOut)
                    if (!hasResult) {
                        hasResult = true
                        con.resume("")
                        globalWebView.stopLoading()
                        globalWebView.pauseTimers()
                    }
                }

            }
        }


    private fun interface BlobIntercept {
        fun handle(blobTextData: String): Boolean
    }

    override suspend fun interceptBlob(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String = withContext(Dispatchers.Main) {
        if (regex.isBlank())
            return@withContext ""

        globalWebView.clearWeb()

        Log.d("WebViewHelper", "interceptBlob start regex $regex")
        val regexE = Regex(regex)
        suspendCoroutine { con ->
            var hasResult = false

            globalWebView.settings.userAgentString = userAgentString

            globalWebView.webViewClient = object : LightweightGettingWebViewClient(regexE, false) {

                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    //提前注入
                    view.evaluateJavascript(blobHook, null)
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    actionJs?.let { view?.executeJavaScriptCode(it) }
                }
            }

            blobIntercept = BlobIntercept {
                val test = regexE.containsMatchIn(it)
                Log.d("WebViewHelper", "Blob res=$test\n$it")
                if (test) {
                    hasResult = true
                    con.resume(it)
                }
                test
            }
            globalWebView.resumeTimers()
            globalWebView.loadUrl(url, header ?: emptyMap())
            launch(Dispatchers.Main) {
                delay(timeOut)
                if (!hasResult) {
                    hasResult = true
                    con.resume("")
                    globalWebView.stopLoading()
                    globalWebView.pauseTimers()
                }
            }
        }
    }
}