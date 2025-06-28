package org.easybangumi.next.shared.plugin.api.component.filter

import org.easybangumi.next.lib.utils.PagingFrame
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
interface FilterComponent: Component {

    suspend fun paramFilter(
        param: String
    ): SourceResult<List<Filter>>

    suspend fun refreshFilter(
        origin: List<Filter>,
        change: Filter,
    ): SourceResult<List<Filter>>


    fun firstKey(
        filterList: List<Filter>
    ): String

    suspend fun search(
        filterList: List<Filter>,
        key: String,
    ): SourceResult<PagingFrame<CartoonCover>>

}