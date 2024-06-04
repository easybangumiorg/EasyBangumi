package com.heyanle.easybangumi4.utils

import android.webkit.WebView
import com.heyanle.easybangumi4.source.utils.LightweightGettingWebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.text.StringEscapeUtils
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by LoliBall on 2023/12/14 22:53.
 * https://github.com/WhichWho
 */

fun WebView.clearWeb() {
    clearHistory()
    clearFormData()
    clearMatches()
}

fun WebView.stop() {
    stopLoading()
    pauseTimers()
}

suspend fun WebView.getHtml(): String {
    val raw = evaluateJavascriptSync("(function() { return document.documentElement.outerHTML })()")
    return withContext(Dispatchers.IO) { StringEscapeUtils.unescapeEcmaScript(raw) }
}

suspend fun WebView.evaluateJavascriptSync(code: String? = null): String {
    if (code.isNullOrEmpty()) return ""
    return suspendCoroutine { con ->
        evaluateJavascript(code) {
            con.resume(it.orEmpty())
        }
    }
}

fun WebView.evaluateJavascript(code: String? = null) {
    if (code.isNullOrEmpty()) return
    evaluateJavascript(code, null)
}

suspend fun WebView.waitUntilLoadFinish(timeoutMs: Long = 8000L) {
    waitUntil(null, timeoutMs)
}

suspend fun WebView.waitUntil(regex: Regex? = null, timeoutMs: Long = 8000L, stopLoading: Boolean = false, ignoreTimeoutExt: Boolean = true): String {
    return try {
        withTimeout(timeoutMs) {
            suspendCoroutine<String> { con ->
                launch(Dispatchers.Main) {
                    webViewClient = object : LightweightGettingWebViewClient(regex) {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            "onPageFinished: $url".logi("WebView.waitUntil")
                            runCatching {
                                if (regex == null) con.resume("")
                            }
                        }

                        override fun onLoadResource(view: WebView?, url: String?) {
                            "onLoadResource: $url".logi("WebView.waitUntil")
                            if (regex != null && url != null && regex.matches(url)) {
                                "onLoadResource: match ${regex}".logi("WebView.waitUntil")
                                runCatching {
                                    con.resume(url)
                                }
                                if(stopLoading){
                                    stopLoading()
                                }
                            }
                        }
                    }
                }
            }.apply {
                "suspendCoroutine ${regex}".logi("WebView.waitUntil")
            }
        }
    } catch (ex: TimeoutCancellationException) {
        ex.printStackTrace()
        if (ignoreTimeoutExt){
            return ""
        }
        throw ex
    }
}
