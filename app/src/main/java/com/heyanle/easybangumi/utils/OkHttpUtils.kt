package com.heyanle.easybangumi.utils

import android.annotation.SuppressLint
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Created by HeYanLe on 2021/10/14 19:44.
 * https://github.com/heyanLE
 */
object OkHttpUtils {

    private val okhttpClient =  OkHttpClient.Builder()
        .hostnameVerifier(createHostnameVerifier())
        .sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
        .build()

    fun get(url: String): String{
        return okhttpClient.newCall(Request.Builder().url(url).header("User-Agent",
            EasyApplication.INSTANCE.getString(R.string.UA)).get().build()).execute().body!!.string()
    }

    private fun createSSLSocketFactory(): SSLSocketFactory{
        return runCatching {
            SSLContext.getInstance("TLS").let {
                it.init(null, arrayOf(TrustAllManager()), SecureRandom())
                it.socketFactory
            }
        }.getOrElse {
            throw it
        }
    }

    private fun createHostnameVerifier(): HostnameVerifier{
        return HostnameVerifier { _, _ -> true }
    }

}

class TrustAllManager : X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}

class TrustAllCerts : X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return arrayOfNulls(0)
    }
}