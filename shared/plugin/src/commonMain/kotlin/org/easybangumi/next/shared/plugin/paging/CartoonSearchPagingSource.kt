package org.easybangumi.next.shared.plugin.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.component.SearchComponent
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
) : PagingSource<String, CartoonCover>() {

    override fun getRefreshKey(state: PagingState<String, CartoonCover>): String? {
        return searchBusiness.runDirect {
            firstKey(keyword)
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CartoonCover> {
        val key = params.key ?: return LoadResult.Error(NullPointerException())
        searchBusiness.run {
            search(keyword, key)
        }.onOK {
            return LoadResult.Page(
                data = it.second,
                prevKey = null,
                nextKey = it.first
            )
        }
            .onError {
                val err = it.error
                return if (err != null) {
                    LoadResult.Error(err)
                } else {
                    LoadResult.Error(Exception(it.msg ?: "load error"))
                }
            }
        return LoadResult.Error(IllegalStateException())
    }
}