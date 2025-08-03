package com.heyanle.easybangumi4.plugin.source.utils.network

import android.content.Context
import android.os.Build
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.plugin.source.utils.network.interceptor.CloudflareInterceptor
import com.heyanle.easybangumi4.plugin.source.utils.network.interceptor.CloudflareUserInterceptor
import com.heyanle.easybangumi4.plugin.source.utils.network.interceptor.CookieInterceptor
import com.heyanle.easybangumi4.plugin.source.utils.network.interceptor.UserAgentInterceptor
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.core.network.GET
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
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
 * Created by heyanlin on 2024/6/4.
 */
class OkhttpHelperImpl(
    private val context: Context,
    private val webViewHelper: WebViewHelper,
    private val networkHelper: NetworkHelper,
    private val webViewHelperV2Impl: WebViewHelperV2Impl,
): OkhttpHelper {

    private val cacheDir = File(context.cacheDir, "network_cache")
    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    private val userAgentInterceptor by lazy { UserAgentInterceptor(networkHelper) }
    private val cloudflareInterceptor by lazy { CloudflareInterceptor(context, networkHelper, webViewHelperV2Impl) }
    private val cloudflareUserInterceptor by lazy { CloudflareUserInterceptor(context, networkHelper,webViewHelperV2Impl) }
    private val cookieInterceptor by lazy { CookieInterceptor(networkHelper) }

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(networkHelper.cookieManager)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(2, TimeUnit.MINUTES)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(cookieInterceptor)
                .sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier(createHostnameVerifier())

            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                }
                builder.addNetworkInterceptor(httpLoggingInterceptor)
            }
            return builder
        }

    private val _client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }
    private val _cloudflareClient by lazy {
        client.newBuilder()
            .addInterceptor(cloudflareInterceptor)
            .build()
    }
    private val _cloudflareWebViewClient by lazy {
        client.newBuilder()
            .addInterceptor(cloudflareUserInterceptor)
            .build()
    }

    override val client: OkHttpClient
        get() = _client
    override val cloudflareClient: OkHttpClient
        get() = _cloudflareClient
    override val cloudflareWebViewClient: OkHttpClient
        get() = _cloudflareWebViewClient

    init {
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