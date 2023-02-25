package com.heyanle.easybangumi4.ui.common.page.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.heyanle.bangumi_source_api.api2.component.page.ListPage
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/25 20:46.
 * https://github.com/heyanLE
 */
class ListPagePagingSource(
    private val listPage: ListPage
) : PagingSource<Int, CartoonCover>() {

    override fun getRefreshKey(state: PagingState<Int, CartoonCover>): Int? {
        return listPage.firstKey()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CartoonCover> {
        val key = params.key ?: return LoadResult.Error(NullPointerException())
        Log.d("ListPagePagingSource", this.toString())
        kotlin.runCatching {
            listPage.getCartoons(key)
                .apply {
                    Log.d("ListPagePagingSource", this.toString())
                }
                .complete {
                    return LoadResult.Page(
                        data = it.data.second,
                        prevKey = null,
                        nextKey = it.data.first
                    )
                }
                .error {
                    return if (it.isParserError) {
                        LoadResult.Error(it.throwable)
                    } else {
                        LoadResult.Error(Exception("load error"))
                    }
                }
        }.onFailure {
            it.printStackTrace()
        }

        return LoadResult.Error(IllegalAccessException())
    }
}