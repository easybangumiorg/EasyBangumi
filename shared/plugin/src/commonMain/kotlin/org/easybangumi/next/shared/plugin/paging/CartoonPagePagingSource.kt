package org.easybangumi.next.shared.plugin.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.component.page.PageComponent
import org.easybangumi.next.shared.plugin.api.component.page.PageTab
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
class CartoonPagePagingSource(
    val pageTab: PageTab,
    val pageBusiness: ComponentBusiness<PageComponent>
) : PagingSource<String, CartoonCover>(){

    override fun getRefreshKey(state: PagingState<String, CartoonCover>): String? {
        return pageTab.initKey
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CartoonCover> {
        val key = params.key ?: return LoadResult.Error(NullPointerException())
        pageBusiness.run {
            loadPage(pageTab, key)
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