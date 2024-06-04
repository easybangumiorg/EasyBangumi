package com.heyanle.easybangumi4.source.utils.network.interceptor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch

/**
 * Created by HeYanLe on 2023/2/3 22:23.
 * https://github.com/heyanLE
 */
class CloudflareInterceptor(
    private val context: Context,
    private val networkHelper: NetworkHelper,
) : WebViewInterceptor(context, networkHelper) {

    private val executor = ContextCompat.getMainExecutor(context)
    override fun shouldIntercept(response: Response): Boolean {
        return response.code in ERROR_CODES && response.header("Server") in SERVER_CHECK
    }

    override fun intercept(
        chain: Interceptor.Chain,
        request: Request,
        response: Response
    ): Response {

        try {
            response.close()

            networkHelper.cookieManager.remove(request.url, COOKIE_NAMES, 0)
            val oldCookie = networkHelper.cookieManager.get(request.url)
                .firstOrNull { it.name == "cf_clearance" }
            resolveWithWebView(request, oldCookie)

            return chain.proceed(request)
        }
        catch (e: CloudflareBypassException) {
            throw IOException(e)
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithWebView(originalRequest: Request, oldCookie: Cookie?) {
        // We need to lock this thread until the WebView finds the challenge solution url, because
        // OkHttp doesn't support asynchronous interceptors.
        val latch = CountDownLatch(1)

        var webview: WebView? = null

        var challengeFound = false
        var cloudflareBypassed = false

        val origRequestUrl = originalRequest.url.toString()
        val headers = parseHeaders(originalRequest.headers)

        executor.execute {
            webview = createWebView(originalRequest)

            webview?.webViewClient = object : WebViewClientCompat() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d("CloudflareInterceptor", "intercept")
                    fun isCloudFlareBypassed(): Boolean {
                        return networkHelper.cookieManager.get(origRequestUrl.toHttpUrl())
                            .firstOrNull { it.name == "cf_clearance" }
                            .let { it != null && it != oldCookie }
                    }

                    if (isCloudFlareBypassed()) {
                        cloudflareBypassed = true
                        latch.countDown()
                    }

                    if (url == origRequestUrl && !challengeFound) {
                        // The first request didn't return the challenge, abort.
                        latch.countDown()
                    }
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceErrorCompat
                ) {
                    onReceivedErrorCompat(
                        view,
                        error.errorCode,
                        error.description?.toString(),
                        request.url.toString(),
                        request.isForMainFrame,
                    )
                }


                @Deprecated("Deprecated in Java")
                final override fun onReceivedError(
                    view: WebView,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String,
                ) {
                    onReceivedErrorCompat(
                        view,
                        errorCode,
                        description,
                        failingUrl,
                        failingUrl == view.url
                    )
                }

                final override fun onReceivedHttpError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceResponse,
                ) {
                    onReceivedErrorCompat(
                        view,
                        error.statusCode,
                        error.reasonPhrase,
                        request.url
                            .toString(),
                        request.isForMainFrame,
                    )
                }

                fun onReceivedErrorCompat(
                    view: WebView,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String,
                    isMainFrame: Boolean,
                ) {
                    if (isMainFrame) {
                        if (errorCode in ERROR_CODES) {
                            // Found the Cloudflare challenge page.
                            challengeFound = true
                        } else {
                            // Unlock thread, the challenge wasn't found.
                            latch.countDown()
                        }
                    }
                }
            }

            webview?.loadUrl(origRequestUrl, headers)
        }

        latch.awaitFor60Seconds()

        executor.execute {
            webview?.run {
                stopLoading()
                destroy()
            }
        }

        if (!cloudflareBypassed) {
            throw CloudflareBypassException()
        }
    }
}

private val ERROR_CODES = listOf(403, 503)
private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
private val COOKIE_NAMES = listOf("cf_clearance")

class CloudflareBypassException : Exception("CloudflareBypassError")