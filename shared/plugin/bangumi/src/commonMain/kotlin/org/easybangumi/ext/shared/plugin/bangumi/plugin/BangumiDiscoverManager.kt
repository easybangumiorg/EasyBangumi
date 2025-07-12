package org.easybangumi.ext.shared.plugin.bangumi.plugin

import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp.Success
import org.easybangumi.next.shared.source.bangumi.model.toCartoonCover
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceException
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverManager

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

class BangumiDiscoverManager(
    private val api: BangumiApi
) : DiscoverManager {

    private val logger = logger()

    override val fromSourceKey: String = BangumiInnerSource.SOURCE_ID

    // 发现页 banner 数据
    suspend fun banner(): DataState<List<CartoonCover>> {
        return api.getBanners().await().toDataState().map {
            it.mapNotNull { it.toCartoonCover() }
        }
    }


    class BangumiTrendsPagingSource(
        private val api: BangumiApi,
        private val trendsFrom: BangumiApi.TrendsFrom,
    ) : EasyPagingSource<CartoonCover> {

        override val initKey: String
            get() = "1"

        override suspend fun load(key: String): DataState<PagingFrame<CartoonCover>> {
            val page = key.toIntOrNull() ?: 1
            val resp = api.getTrends(page, trendsFrom).await().toDataState()
            return  resp.map { ts ->
                val coverList = ts.mapNotNull {
                    it.toCartoonCover()
                }
                if (coverList.isEmpty()) {
                    null to coverList
                } else {
                    (page + 1).toString() to coverList
                }
            }
        }
    }

    // 发现页推荐
    fun trendsPagingSource(
        trendsFrom: BangumiApi.TrendsFrom,
    ): EasyPagingSource<CartoonCover> {
        return BangumiTrendsPagingSource(api, trendsFrom)
    }

    private fun <T> BgmRsp<T>.getOrThrow(): T {
        return when (this) {
            is Success -> data
            is BgmRsp.Error -> throw SourceException("业务错误：${code} ${url} ${throwable?.message ?: throwable?.let { it::class.simpleName }}", throwable)
        }
    }
}