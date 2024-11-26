package com.heyanle.easybangumi4.utils

import android.webkit.WebView
import com.heyanle.easybangumi4.plugin.source.utils.LightweightGettingWebViewClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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
private val scope = MainScope()
suspend fun WebView.waitUntil(
    regex: Regex? = null,
    timeoutMs: Long = 8000L,
    stopLoading: Boolean = false,
    ignoreTimeoutExt: Boolean = true
): String {
    val res = withTimeoutOrNull(timeoutMs) {
        return@withTimeoutOrNull suspendCancellableCoroutine<String> { con ->
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
                        if (stopLoading) {
                            stopLoading()
                        }
                    }
                }
            }
        }
    }
        ?: if (ignoreTimeoutExt) {
            return ""
        } else {
            throw CancellationException()
        }
    res.logi("WebView.waitUtil")
    return res

}
