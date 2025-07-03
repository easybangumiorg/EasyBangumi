package org.easybangumi.ext.shared.plugin.bangumi.plugin

import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceException
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.BaseComponent
import org.easybangumi.next.shared.plugin.api.component.discover.BannerHeadline
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.discover.RecommendTab
import org.easybangumi.next.shared.plugin.api.withResult
import org.easybangumi.ext.shared.plugin.bangumi.business.BangumiApi
import org.easybangumi.ext.shared.plugin.bangumi.business.BangumiBusiness
import org.easybangumi.ext.shared.plugin.bangumi.business.embed.BangumiRankingEmbedProxyHandler
import org.easybangumi.ext.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.ext.shared.plugin.bangumi.model.BgmRsp.Success
import org.easybangumi.ext.shared.plugin.bangumi.model.toCartoonCover
import org.koin.core.component.inject

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
class BangumiDiscoverComponent: DiscoverComponent, BaseComponent() {

    private val logger = logger()

    override fun bannerHeadline() = BannerHeadline(
        label = "最高热度",
        hasTimelineEnter = true
    )

    override suspend fun banner(): SourceResult<List<CartoonCover>> {
        return withResult {
            val business: BangumiBusiness by inject()
            val resp = business.api.getBanners().await()
            resp.getOrThrow().apply {
                logger.info("获取 Banner 数据成功，数量: ${size}")
            }.mapNotNull {
                it.toCartoonCover()
            }.toList()
        }
    }

    override suspend fun recommendTab(): SourceResult<List<RecommendTab>> {
        return withResult {
            listOf(
                RecommendTab(
                    id = BangumiApi.TrendsFrom.ORIGINAL.path,
                    name = BangumiApi.TrendsFrom.ORIGINAL.label,
                    initKey = "1"
                ),
                RecommendTab(
                    id = BangumiApi.TrendsFrom.MANGA.path,
                    name = BangumiApi.TrendsFrom.MANGA.label,
                    initKey = "1"
                ),
                RecommendTab(
                    id = BangumiApi.TrendsFrom.NOVEL.path,
                    name = BangumiApi.TrendsFrom.NOVEL.label,
                    initKey = "1"
                ),
            )
        }
    }

    override suspend fun loadRecommend(
        tab: RecommendTab,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>> {
        return withResult {
            val business: BangumiBusiness by inject()
            val resp = business.api.getTrends(
                from = BangumiApi.TrendsFrom.entries.find { it.path == tab.id } ?: throw IllegalStateException("未知的tab: ${tab.id}"),
                page = key.toIntOrNull() ?: 1
            ).await()
            val data = resp.getOrThrow()
            val nextKey = if (data.isEmpty()) null else ((key.toIntOrNull() ?: 1) + 1)
            nextKey?.toString() to data.mapNotNull { it.toCartoonCover() }.toList()
        }
    }

    private fun <T> BgmRsp<T>.getOrThrow(): T {
        return when (this) {
            is Success -> data
            is BgmRsp.Error -> throw SourceException("业务错误：${code} ${throwable?.message ?: throwable?.let { it::class.simpleName }}", throwable)
        }
    }
}