package com.heyanle.lib_anim.utils.network

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.heyanle.lib_anim.utils.getDefaultUserAgentString
import com.heyanle.lib_anim.utils.network.interceptor.CloudflareInterceptor
import com.heyanle.lib_anim.utils.network.interceptor.CloudflareUserInterceptor
import com.heyanle.lib_anim.utils.network.interceptor.CookieInterceptor
import com.heyanle.lib_anim.utils.network.interceptor.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by HeYanLe on 2023/2/3 21:53.
 * https://github.com/heyanLE
 */

lateinit var networkHelper: NetworkHelper

class NetworkHelper(
    context: Context,
    private val isDebug: Boolean = false,
) {
    private val cacheDir = File(context.cacheDir, "network_cache")
    private val cacheSize = 5L * 1024 * 1024 // 5 MiB


    val cookieManager = AndroidCookieJar()
    val defaultLinuxUA =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.76"
    val defaultUA:String = kotlin.runCatching {
        WebView(context).getDefaultUserAgentString()
    }.getOrElse {
        it.printStackTrace()
        "Mozilla/5.0 (Linux; Android 13; RMX3551 Build/SKQ1.220617.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/103.0.5060.129 Mobile Safari/537.36"

    }
    private val userAgentInterceptor by lazy { UserAgentInterceptor(this) }
    private val cloudflareInterceptor by lazy { CloudflareInterceptor(context, this) }
    private val cloudflareUserInterceptor by lazy { CloudflareUserInterceptor(context, this) }
    private val cookieInterceptor by lazy { CookieInterceptor(this) }

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(cookieManager)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(2, TimeUnit.MINUTES)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(cookieInterceptor)
                .sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier(createHostnameVerifier())

            if (isDebug) {
                val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                }
                builder.addNetworkInterceptor(httpLoggingInterceptor)
            }
            return builder
        }

    val client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }

    @Suppress("UNUSED")
    val cloudflareClient by lazy {
        client.newBuilder()
            .addInterceptor(cloudflareInterceptor)
            .build()
    }

    @Suppress("UNUSED")
    val cloudflareUserClient by lazy {
        client.newBuilder()
            .addInterceptor(cloudflareUserInterceptor)
            .build()
    }

    private fun createSSLSocketFactory(): SSLSocketFactory {
        return runCatching {
            SSLContext.getInstance("TLS").let {
                it.init(null, arrayOf(TrustAllManager()), SecureRandom())
                it.socketFactory
            }
        }.getOrElse {
            throw it
        }
    }

    private fun createHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ -> true }
    }

}

class TrustAllManager : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}

class TrustAllCerts : X509TrustManager {
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return arrayOfNulls(0)
    }
}