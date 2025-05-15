package org.easybangumi.next.shared.plugin.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.component.SearchComponent
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness

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
class CartoonSearchPagingSource(
    val keyword: String,
    val searchBusiness: ComponentBusiness<SearchComponent>
) : EasyPagingSource<CartoonCover> {

    override val initKey: String = searchBusiness.runDirect {
        firstKey(keyword)
    }

    override suspend fun load(key: String): DataState<PagingFrame<CartoonCover>> {
        return searchBusiness.run {
            search(keyword, key)
        }.toDataState()
    }
}