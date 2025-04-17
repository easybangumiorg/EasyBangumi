package org.easybangumi.next.shared.plugin.api.component.discover

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
data class DiscoverColum (
    val label: String,
    val jumpTitle: String,

    // 可以跳转到 历史记录页，收藏页选定特定分类，筛选页选定特定筛选，搜索页搜索特定关键字
    val jumpRouter: String,
    val coverList: List<CartoonCover>
) {

}