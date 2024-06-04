package com.heyanle.easybangumi4.source.utils.network.interceptor

import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by heyanlin on 2024/6/4.
 */
class UserAgentInterceptor(
    private val networkHelper: NetworkHelper
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        return if (originalRequest.header("User-Agent").isNullOrEmpty()) {
            val newRequest = originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", networkHelper.randomUA)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}