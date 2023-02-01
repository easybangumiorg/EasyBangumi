package com.heyanle.easybangumi.source.utils;

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.webkit.*
import androidx.annotation.CallSuper
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import com.heyanle.bangumi_source_api.api.utils.WebUtils
import com.heyanle.bangumi_source_api.api.utils.webUtil
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.utils.containStrs
import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
object WebUtilImpl : WebUtils {

   val cookieManager = CookieManager.getInstance()

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
    private var blobIntercept: BlobIntercept? = null

    val globalWebView by lazy(LazyThreadSafetyMode.NONE) {
        WebView(BangumiApp.INSTANCE).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT

                // Allow zooming
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
            cookieManager.also {
                it.setAcceptCookie(true)
                it.acceptCookie()
                // 跨域cookie读取
                it.setAcceptThirdPartyCookies(this, true)
            }
            //BlobHook回调
//            addJavascriptInterface(object : Any() {
//                @JavascriptInterface
//                fun handleWrapper(blobTextData: String) {
//                    if (blobIntercept?.handle(blobTextData) == true) {
//                        //每次handle一次性
//                        blobIntercept = null
//                    }
//                }
//            }, "blobHook")
        }
    }

    private val cb = ValueCallback<Boolean> { }
    fun WebView.clearWeb() {
        clearHistory()
        clearFormData()
        clearMatches()
        cookieManager.apply {
            removeSessionCookies(cb)
            removeAllCookies(cb)
        }
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



        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            Log.d("WebUtilImpl", request?.url.toString() + " -> " + request?.requestHeaders?.get("Cookie"))

            val resp =  super.shouldInterceptRequest(view, request)
            Log.d("WebUtilImpl", request?.url.toString() + " <- " + resp?.responseHeaders?.get("Set-Cookie"))
            return resp
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
        actionJs: String?,
        timeOut: Long
    ): String {
        return withContext(Dispatchers.Main) {

            globalWebView.clearWeb()

            suspendCoroutine { con ->
                Log.d("开始获取源码", url)
                val regexE = Regex(callBackRegex)
                var hasResult = false

                fun callBack(web: WebView) {
                    hasResult = true

                    web.executeJavaScriptCode(actionJs ?: "")
                    web.evaluateJavascript("(function() { return document.documentElement.outerHTML })()") {
                        Log.d("脚本返回", url)
                        if (it.isNullOrEmpty())
                            con.resume("")
                        else {
                            launch(Dispatchers.Default) {
                                val source = StringEscapeUtils.unescapeEcmaScript(it)
                                Log.d("获取源码成功", source)
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


                        Log.d("页面载入完成", url ?: "")
                        if (!hasResult && callBackRegex.isBlank())
                            callBack(view)
                    }

                    override fun onLoadResource(view: WebView, url: String) {
                        url?.let {
                            cookieManager.getCookie("https://www.yhdmp.net/")?.moeSnackBar()
                        }
                        Log.d("1.链接", url)
                        if (callBackRegex.isNotBlank() && !hasResult && regexE.matches(url)) {
                            Log.d("匹配到回调", url)
                            callBack(view)
                        }
                        super.onLoadResource(view, url)
                    }
                }
                globalWebView.resumeTimers()
                globalWebView.loadUrl(url)
                launch(Dispatchers.Main) {
                    delay(timeOut)
                    Log.d("获取源码", "${timeOut}ms超时返回")
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
        actionJs: String?,
        timeOut: Long
    ): String =
        withContext(Dispatchers.Main) {
            Log.d("开始拦截请求", "正则:$regex")

            globalWebView.clearWeb()

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
                        Log.d("2.链接", url)
                        if (!hasResult && regexE.matches(url)) {
                            Log.d("匹配到资源", url)
                            con.resume(url)
                            hasResult = true
                            view?.stopLoading()
                            view?.pauseTimers()
                        }
                        super.onLoadResource(view, url)
                    }
                }
                globalWebView.resumeTimers()
                globalWebView.loadUrl(url)
                launch(Dispatchers.Main) {
                    delay(timeOut)
                    if (!hasResult) {
                        Log.d("拦截资源", "${timeOut}ms超时返回")
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
        actionJs: String?,
        timeOut: Long
    ): String = withContext(Dispatchers.Main) {
        if (regex.isBlank())
            return@withContext ""

        globalWebView.clearWeb()

        Log.d("开始拦截Blob", "正则:$regex")
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
                Log.d("Blob数据", "res=$test\n$it")
                if (test) {
                    Log.d("匹配Blob数据", "\n" + it,)
                    hasResult = true
                    con.resume(it)
                }
                test
            }
            globalWebView.resumeTimers()
            globalWebView.loadUrl(url)
            launch(Dispatchers.Main) {
                delay(timeOut)
                if (!hasResult) {
                    Log.d("拦截Blob", "${timeOut}ms超时返回")
                    hasResult = true
                    con.resume("")
                    globalWebView.stopLoading()
                    globalWebView.pauseTimers()
                }
            }
        }
    }

}