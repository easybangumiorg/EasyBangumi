package org.easybangumi.next.shared.source.bangumi

import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.pathProvider

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
data class BangumiConfig(
    val apiHost: String = DEFAULT_BANGUMI_API_HOST,
    val authApiHost: String = DEFAULT_BANGUMI_API_HOST,
    val htmlHost: String = DEFAULT_BANGUMI_HTML_HOST,
    val embedProxyHost: String = BANGUMI_EMBED_PROXY_HOST,
    val authApi: String = DEFAULT_BANGUMI_AUTH_API_HOST,
    val cachePath: UFD = pathProvider.getCachePath("bangumi"),

    val appId: String,
    val appSecret: String,
    val callbackUrl: String,

    val handler: BangumiHandler,
) {


    interface BangumiHandler {

        fun onAuthFailed()

    }
    companion object {
        const val DEFAULT_BANGUMI_API_HOST = "api.bgm.tv"
        const val DEFAULT_BANGUMI_HTML_HOST = "chii.in"

        const val BANGUMI_EMBED_PROXY_HOST = "bangumi.embed.proxy"

        const val DEFAULT_BANGUMI_AUTH_API_HOST = "bgm.tv"
    }


    fun makeUrl(src: String): String {
        var res = src
        if (res.startsWith("//")) {
            res = "https:$res"
        } else if (res.startsWith("/")) {
            res = "https://$htmlHost$res"
        }
        return res
    }

}