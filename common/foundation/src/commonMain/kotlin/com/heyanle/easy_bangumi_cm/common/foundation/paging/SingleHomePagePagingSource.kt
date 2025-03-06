package com.heyanle.easy_bangumi_cm.common.foundation.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage

/**
 * Created by heyanlin on 2025/3/5.
 */
class SingleHomePagePagingSource(
    private val homePage: HomePage.SingleCartoonPage
) : PagingSource<Int, CartoonCover>() {

    override fun getRefreshKey(state: PagingState<Int, CartoonCover>): Int {
        return homePage.firstKey()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CartoonCover> {
        val key = params.key ?: return LoadResult.Error(NullPointerException())
        kotlin.runCatching {
            homePage.load(key)
                .onOK {
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
        }.onFailure {
            it.printStackTrace()
        }

        return LoadResult.Error(IllegalAccessException())

    }
}