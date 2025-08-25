package org.easybangumi.next.shared.source.api.component.search

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.play.IPlayComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent


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

class SearchPagingSource(
    private val searchBusiness: ComponentBusiness<SearchComponent>,
    private val keyword: String,
): EasyPagingSource<CartoonCover> {

    override val initKey: String
        get() = searchBusiness.runDirect { firstKey() }

    override suspend fun load(key: String): DataState<PagingFrame<CartoonCover>> {
       return searchBusiness.run {
           search(keyword, key)
       }
    }
}

fun ComponentBusiness<SearchComponent>.createPagingSource(keyword: String): EasyPagingSource<CartoonCover> {
    return SearchPagingSource(this, keyword)
}