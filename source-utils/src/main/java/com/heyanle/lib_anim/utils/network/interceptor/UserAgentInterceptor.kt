package com.heyanle.lib_anim.utils.network.interceptor

import com.heyanle.lib_anim.utils.network.NetworkHelper
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by HeYanLe on 2023/2/3 22:02.
 * https://github.com/heyanLE
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
                .addHeader("User-Agent", networkHelper.defaultUA)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}