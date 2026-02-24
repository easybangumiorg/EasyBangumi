package com.heyanle.easybangumi4.source_api.utils.api

import com.heyanle.easybangumi4.source_api.utils.core.AndroidCookieJar

/**
 * Created by HeYanLe on 2023/10/18 23:51.
 * https://github.com/heyanLE
 */
interface NetworkHelper {

    val cookieManager: AndroidCookieJar
    val defaultLinuxUA: String
    val defaultAndroidUA: String
    val randomUA: String

}