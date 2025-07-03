package org.easybangumi.ext.shared.plugin.bangumi.business

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
    val bangumiApiHost: String = DEFAULT_BANGUMI_API_HOST,
    val bangumiHtmlHost: String = DEFAULT_BANGUMI_HTML_HOST,
    val bangumiEmbedProxyHost: String = BANGUMI_EMBED_PROXY_HOST,
) {

    companion object {
        const val DEFAULT_BANGUMI_API_HOST = "api.bgm.tv"
        const val DEFAULT_BANGUMI_HTML_HOST = "chii.in"
        const val BANGUMI_EMBED_PROXY_HOST = "bangumi.embed.proxy"
    }


    fun makeUrl(src: String): String {
        var res = src
        if (res.startsWith("//")) {
            res = "https:$res"
        } else if (res.startsWith("/")) {
            res = "https://$bangumiHtmlHost$res"
        }
        return res
    }

}