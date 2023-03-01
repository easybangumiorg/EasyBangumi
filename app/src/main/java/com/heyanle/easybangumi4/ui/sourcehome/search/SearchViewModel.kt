package com.heyanle.easybangumi4.ui.sourcehome.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.flow.flow

/**
 * Created by HeYanLe on 2023/3/1 16:09.
 * https://github.com/heyanLE
 */
class SearchViewModel(
    private val searchSource: SearchComponent
): ViewModel() {

    companion object {
        val empty = flow <PagingData<CartoonCover>> { PagingData.empty<CartoonCover>() }
    }

    val isEmpty = mutableStateOf(true)
    val curPager = mutableStateOf(empty)

    fun search(keyword: String){
        if(keyword.isEmpty()){
            isEmpty.value = true
            curPager.value = empty
        }else{
            isEmpty.value = false
            curPager.value = getPager(keyword).flow.cachedIn(viewModelScope)
            keyword.logi("SearchViewModel")
        }

    }

    private fun getPager(keyword: String): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = searchSource.getFirstSearchKey(keyword)
        ) {
            PagingSearchSource(searchSource, keyword)
        }
    }

}

class SearchViewModelFactory(
    private val searchSource: SearchComponent
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java))
            return SearchViewModel(searchSource) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}