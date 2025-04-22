package org.easybangumi.next.shared.plugin.debug

import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.SearchComponent
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverColum
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.filter.Filter
import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
import org.easybangumi.next.shared.plugin.api.withResult
import org.easybangumi.next.shared.plugin.core.component.ComponentWrapper

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
class HomeComponent: ComponentWrapper(), DiscoverComponent, FilterComponent, SearchComponent {

    override suspend fun banner(): SourceResult<List<CartoonCover>> {
        return withResult {
            DebugConst.cartoonCoverTest.toList()
        }
    }

    override suspend fun column(): SourceResult<List<DiscoverColum>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshFilter(
        origin: List<Filter>,
        change: Filter?
    ): SourceResult<List<Filter>> {
        TODO("Not yet implemented")
    }

    override suspend fun firstKey(filterList: List<Filter>): SourceResult<String> {
        TODO("Not yet implemented")
    }

    override suspend fun firstKey(keyword: String): SourceResult<String> {
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

    private fun newCartoonCover(){

    }
}