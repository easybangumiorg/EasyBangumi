package org.easybangumi.next.shared.plugin.api.component.discover

import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.Component


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

interface DiscoverComponent: Component {

    // 必须直接返回，不能做其他耗时操作
    fun bannerHeadline(): BannerHeadline

    suspend fun banner(): SourceResult<List<CartoonCover>>

    suspend fun recommendTab(): SourceResult<List<RecommendTab>>

    suspend fun loadRecommend(
        tab: RecommendTab,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>>

}