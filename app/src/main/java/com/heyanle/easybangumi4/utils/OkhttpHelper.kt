package com.heyanle.easybangumi4.utils

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.base.DataResult
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException

/**
 * Created by HeYanLe on 2023/4/3 23:03.
 * https://github.com/heyanLE
 */
object OkhttpHelper {

    private val cacheDir: File by lazy {
        File(APP.cacheDir, "network_cache")
    }
    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()

            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                }
                builder.addNetworkInterceptor(httpLoggingInterceptor)
            }
            return builder
        }

    val client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }


}