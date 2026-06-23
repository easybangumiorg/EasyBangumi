package com.heyanle.easybangumi4.ui.search_migrate

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.utils.logi

/**
 * Created by HeYanLe on 2023/3/1 16:15.
 * https://github.com/heyanLE
 */
class PagingSearchSource(
    private val searchParser: SearchComponent,
    private val keyword: String,
    private val verificationProvider: (key: Int, keyword: String) -> VerificationResult? = {_,_ -> null}
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
            val verificationResult = verificationProvider(key, keyword)
            if (verificationResult != null) {
                searchParser.search(key, keyword, verificationResult)
            } else {
                searchParser.search(key, keyword)
            }.let {
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
