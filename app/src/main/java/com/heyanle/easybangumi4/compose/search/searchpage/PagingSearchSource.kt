package com.heyanle.easybangumi4.compose.search.searchpage

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.utils.logi

/**
 * Created by HeYanLe on 2023/3/1 16:15.
 * https://github.com/heyanLE
 */
class PagingSearchSource(
    private val searchParser: SearchComponent,
    private val keyword: String,
): PagingSource<Int, CartoonCover>() {

    override fun getRefreshKey(state: PagingState<Int, CartoonCover>): Int? {
        return searchParser.getFirstSearchKey(keyword)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CartoonCover> {
        val key = params.key ?: return LoadResult.Error(NullPointerException())
        if (keyword.isEmpty()) {
            return LoadResult.Error(NullPointerException())
        }
        try {
            searchParser.search(key, keyword).let {
                it.logi("PagingSearchSource")
                return when (it) {
                    is SourceResult.Error -> {
                        it.throwable.printStackTrace()
                        if (it.isParserError) {
                            LoadResult.Error(it.throwable)
                        } else {
                            LoadResult.Error(Exception("load error"))
                        }

                    }

                    is SourceResult.Complete -> {
                        LoadResult.Page(
                            data = it.data.second,
                            prevKey = null,
                            nextKey = it.data.first
                        )
                    }
                }
            }
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}