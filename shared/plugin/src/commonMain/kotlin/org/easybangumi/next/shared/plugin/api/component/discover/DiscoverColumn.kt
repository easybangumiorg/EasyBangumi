package org.easybangumi.next.shared.plugin.api.component.discover

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
data class DiscoverColumn (
    val id: String,
    val label: String,
    // 【查看更多】区域自定义文案，为空则不展示
    val jumpTitle: String = "",

    // 【查看更多】区域跳转
    val jumpRouter: DiscoverColumnJumpRouter,
)

sealed class DiscoverColumnJumpRouter {
    // 跳转筛选页，param 会交给 FilterComponent 解析
    data class Filter(val param: String? = null) : DiscoverColumnJumpRouter()

    // 跳转到搜索页，keyword 为关键词
    data class Search(val keyword: String) : DiscoverColumnJumpRouter()

    // 跳转到自定义页面， pageId 为页面 ID，tabId 为 Tab ID
    data class Page(val pageId: String, val tabId: String = "") : DiscoverColumnJumpRouter()

}