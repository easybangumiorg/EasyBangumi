package com.heyanle.lib_anim.utils

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Created by HeYanLe on 2021/10/14 19:44.
 * https://github.com/heyanLE
 */
object OkHttpUtils {

    val ua =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36 Edg/93.0.961.52"

    private val okhttpClient = OkHttpClient.Builder()
        .hostnameVerifier(createHostnameVerifier())
        .sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
        .build()

    fun get(url: String): String {
        return okhttpClient.newCall(
            Request.Builder().url(url).header(
                "User-Agent",
                ua
            ).get().build()
        ).execute().body!!.string()
    }

    fun post(url: String, body: MultipartBody): String {
        return okhttpClient.newCall(
            Request.Builder().url(url)
                .method("POST", body)
                .addHeader("User-Agent", ua)
                .build()
        ).execute().body!!.string()
    }

    fun getPostFormBody(): MultipartBody.Builder {
        return MultipartBody.Builder().setType(MultipartBody.FORM)
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