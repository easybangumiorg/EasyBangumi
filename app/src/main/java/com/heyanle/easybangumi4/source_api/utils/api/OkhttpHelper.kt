package com.heyanle.easybangumi4.source_api.utils.api

import okhttp3.OkHttpClient

/**
 * Created by HeYanLe on 2023/10/19 0:25.
 * https://github.com/heyanLE
 */
interface OkhttpHelper {

    val client: OkHttpClient
    val cloudflareClient: OkHttpClient
    val cloudflareWebViewClient: OkHttpClient

}