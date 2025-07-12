package org.easybangumi.next.shared.source.bangumi.business.embed

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.util.AttributeKey
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import org.easybangumi.next.shared.source.bangumi.business.BangumiConfig
import org.easybangumi.next.lib.logger.logger

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
 *
 * Bangumi 现有开发者 api 有一些没有覆盖的需求，需要爬 Bangumi 页面解析。
 * Bangumi 页面没有前后端分离，需要直接解析 html
 * 对于这部分需求，新增嵌入代理实现
 * 使用 bangumi.proxy 域名代理请求
 * 例如：
 * 普通 api 业务：api.bgm.tv/calendar -> bangumi ktor client -> json 解析 -> BgmRsp
 * 嵌入代理业务：bangumi.proxy/banners -> bangumi ktor client -> bangumi embed proxy -> chii.in/anime/browser/tv/?sort=trends -> html 解析 -> BgmRsp
 * @see org.easybangumi.next.shared.source.bangumi.business.BangumiApi
 */
class BangumiEmbedProxy(
    private val config: BangumiConfig,
) {


    companion object {
        val embedProxyHandlerAttrKey = AttributeKey<EmbedProxyHandler>("proxyHandler")
    }

    private val logger = logger()

    private val handlerList = listOf<EmbedProxyHandler>(
        // 排行榜相关代理
        BangumiRankingEmbedProxyHandler(config),
        // 评论代理
        BangumiReviewsEmbedProxyHandler(config),
    )

    val bangumHtmlProxyPlugin = createClientPlugin("bangumi_html_proxy") {
        onRequest { req, _ ->
            if (req.url.host.equals(config.bangumiEmbedProxyHost)) {
                val path = req.url.pathSegments.firstOrNull()
                logger.info(path)
                if (path.isNullOrEmpty()) {
                    return@onRequest
                }
                for (handler in handlerList) {
                    if (handler.onReq(req)) {
                        req.attributes.put(embedProxyHandlerAttrKey, handler)
                        return@onRequest
                    }
                }
                throw IllegalArgumentException("BangumiEmbedProxy: no handler found for path: $path")
            }
        }
        transformResponseBody { response: HttpResponse,
                                content: ByteReadChannel,
                                requestedType: TypeInfo ->
            val proxyHandler = response.request.attributes.getOrNull(embedProxyHandlerAttrKey)
            proxyHandler?.onResp(this, response, content, requestedType)
        }
    }



}