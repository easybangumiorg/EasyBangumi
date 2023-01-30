package com.heyanle.lib_anim.utils

import okhttp3.*
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
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"

//    private val cookieStore: HashMap<String, List<Cookie>> = HashMap()

    private val okhttpClient = OkHttpClient.Builder()
        .hostnameVerifier(createHostnameVerifier())
        .sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
//        .cookieJar(object : CookieJar {
//            override fun saveFromResponse(httpUrl: HttpUrl, list: List<Cookie>) {
//                cookieStore[httpUrl.host] = list
//            }
//
//            override fun loadForRequest(httpUrl: HttpUrl): List<Cookie> {
//                val cookies: List<Cookie>? = cookieStore[httpUrl.host]
//                return cookies ?: ArrayList<Cookie>()
//            }
//        })
        .build()

    fun get(url: String): String {
        return okhttpClient.newCall(
            Request.Builder().url(url)
                .addHeader("User-Agent", ua)
                .get().build()
        ).execute().body!!.string()
    }

    fun get(request: Request): String {
        return  okhttpClient
            .newCall(request)
            .execute()
            .body!!.string()
    }

    fun client(): OkHttpClient {
        return okhttpClient
    }

    fun request(url: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .addHeader("User-Agent", ua)
    }

    fun post(url: String, body: MultipartBody): String {
        return okhttpClient.newCall(
            Request.Builder().url(url)
                .method("POST", body)
                .addHeader("User-Agent", ua)
                .build()
        ).execute().body!!.string()
    }

    fun postFormBody(): MultipartBody.Builder {
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