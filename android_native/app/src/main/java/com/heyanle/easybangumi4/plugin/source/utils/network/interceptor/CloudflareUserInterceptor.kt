package com.heyanle.easybangumi4.plugin.source.utils.network.interceptor

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

import com.heyanle.easybangumi4.ui.common.moeSnackBar

/**
 * Created by HeYanLe on 2023/2/4 14:03.
 * https://github.com/heyanLE
 */
class CloudflareUserInterceptor(
    private val context: Context,
    private val networkHelper: NetworkHelper,
    private val webViewHelperV2Impl: WebViewHelperV2Impl,
) : WebViewInterceptor(context, networkHelper, webViewHelperV2Impl) {

    private val executor = ContextCompat.getMainExecutor(context)
    override fun shouldIntercept(response: Response): Boolean {
        // Check if Cloudflare anti-bot is on
        return response.code in ERROR_CODES && response.header("Server") in SERVER_CHECK
    }

    override fun intercept(
        chain: Interceptor.Chain,
        request: Request,
        response: Response
    ): Response {

        stringRes(com.heyanle.easy_i18n.R.string.need_cloudflare_please_wait).moeSnackBar()
        try {
            response.close()
            networkHelper.cookieManager.remove(request.url, COOKIE_NAMES, 0)
            val oldCookie = networkHelper.cookieManager.get(request.url)
                .firstOrNull { it.name == "cf_clearance" }
            resolveWithWebView(request, oldCookie)

            return chain.proceed(request)
        }
        // Because OkHttp's enqueue only handles IOExceptions, wrap the exception so that
        // we don't crash the entire app
        catch (e: CloudflareBypassException) {
            throw IOException(e)
        } catch (e: WaitWebViewException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveWithWebView(originalRequest: Request, oldCookie: Cookie?) {
        // We need to lock this thread until the WebView finds the challenge solution url, because
        // OkHttp doesn't support asynchronous interceptors.

        var webview: WebView? = null

        val origRequestUrl = originalRequest.url.toString()
        val headers = parseHeaders(originalRequest.headers)

        fun isCloudFlareBypassed(): Boolean {
            return networkHelper.cookieManager.get(origRequestUrl.toHttpUrl())
                .firstOrNull { it.name == "cf_clearance" }
                .let { it != null && it != oldCookie }
        }

        executor.execute {
            webview = getWebViewOrThrow(originalRequest)
            webview?.loadUrl(origRequestUrl, headers)

            webview?.let {
                stringRes(com.heyanle.easy_i18n.R.string.please_cloudflare).moeSnackBar()
                webViewHelperV2Impl.openWebPage(
                    it,
                    {
                        isCloudFlareBypassed()
                    },
                    {
                        if (isCloudFlareBypassed()) {
                            stringRes(com.heyanle.easy_i18n.R.string.cloudflare_completely).moeSnackBar()
                        }
                        GlobalScope.launch(Dispatchers.IO) {
                            networkHelper.cookieManager.flush()
                        }
                    }
                )
            }
        }


        throw WaitWebViewException()


    }
}

private val ERROR_CODES = listOf(403, 503)
private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
private val COOKIE_NAMES = listOf("cf_clearance")

class WaitWebViewException : Exception("Wait WebView")