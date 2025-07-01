package org.easybangumi.next.shared.plugin.debug

import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.SearchComponent
import org.easybangumi.next.shared.plugin.api.component.discover.BannerHeadline
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.discover.RecommendTab
import org.easybangumi.next.shared.plugin.api.component.filter.Filter
import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
import org.easybangumi.next.shared.plugin.api.withResult
import org.easybangumi.next.shared.plugin.api.component.BaseComponent

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
class HomeBaseComponent: BaseComponent(), DiscoverComponent, FilterComponent, SearchComponent {

    override suspend fun banner(): SourceResult<List<CartoonCover>> {
        return withResult {
            val arrayList = arrayListOf<CartoonCover>()
            repeat(5) {
                DebugConst.cartoonCoverTest.forEach {
                    arrayList.add(it.copy())
                }
            }
            arrayList

        }
    }

    override fun bannerHeadline(): BannerHeadline {
        return BannerHeadline(
            "新番推荐",
            hasTimelineEnter = true
        )
    }

    override suspend fun recommendTab(): SourceResult<List<RecommendTab>> {
        return withResult {
            val arrayList = arrayListOf<RecommendTab>()
            arrayList.add(RecommendTab(
                "lastest_recommend",
                "新番推荐",
                "1"
            ))
            arrayList.add(RecommendTab(
                "lastest",
                "近期热播",
                "1"
            ))
            arrayList
        }
    }

    override suspend fun loadRecommend(
        tab: RecommendTab,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>> {
        return withResult {
            val page = key.toIntOrNull()
            val arrayList = arrayListOf<CartoonCover>()
            repeat(5) {
                DebugConst.cartoonCoverTest.forEach {
                    arrayList.add(it.copy())
                }
            }
            Pair(if (page != null && page < 10) (page + 1).toString() else null, arrayList)
        }
    }

    override suspend fun paramFilter(param: String): SourceResult<List<Filter>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshFilter(
        origin: List<Filter>,
        change: Filter
    ): SourceResult<List<Filter>> {
        TODO("Not yet implemented")
    }

    override fun firstKey(filterList: List<Filter>): String {
        TODO("Not yet implemented")
    }

    override fun firstKey(): String {
        TODO("Not yet implemented")
    }

    override suspend fun search(
        filterList: List<Filter>,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>> {
        TODO("Not yet implemented")
    }

    override suspend fun search(
        keyword: String,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>> {
        TODO("Not yet implemented")
    }
}