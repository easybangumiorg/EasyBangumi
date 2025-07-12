package org.easybangumi.next.shared.plugin.api.component.discover.common

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonCover

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

interface DiscoverCommonManager {


    // 必须直接返回，不能做其他耗时操作
    fun bannerHeadline(): BannerHeadline

    suspend fun banner(): DataState<List<CartoonCover>>

    suspend fun recommendTab(): DataState<List<RecommendTab>>

    suspend fun loadRecommend(
        tab: RecommendTab,
        key: String
    ): DataState<Pair<String?, List<CartoonCover>>>
}