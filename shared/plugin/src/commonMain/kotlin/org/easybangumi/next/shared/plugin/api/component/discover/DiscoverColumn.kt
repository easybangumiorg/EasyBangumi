package org.easybangumi.next.shared.plugin.api.component.discover

import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex

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
data class DiscoverColum (
    val id: String,
    val label: String,
    // 【查看更多】区域自定义文案，为空则不展示
    val jumpTitle: String = "",

    // 【查看更多】区域跳转
    val jumpRouter: DiscoverColumnJumpRouter,
)

sealed class DiscoverColumnJumpRouter {
    data class Filter(val param: String? = null) : DiscoverColumnJumpRouter()
    data class Search(val keyword: String) : DiscoverColumnJumpRouter()
    data class Page(val pageId: String, val tabId: String) : DiscoverColumnJumpRouter()

}