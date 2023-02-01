package com.heyanle.easybangumi.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.bangumi_source_api.api.ISearchParser
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.easybangumi.ui.search.paging.SearchPageSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/17 16:47.
 * https://github.com/heyanLE
 */
class SearchPageViewModel(
    private val searchParser: ISearchParser,
) : ViewModel() {

    var isCurLast = false

    object EmptyBangumi : SearchPageState.Empty<Int, Bangumi>()
    sealed class SearchPageState<K : Any, V : Any> {
        open class Empty<K : Any, V : Any> : SearchPageState<K, V>() {
            override fun toString(): String {
                return "SearchPageState.Empty"
            }
        }

        class Page<K : Any, V : Any>(val keyword: String, val flow: Flow<PagingData<V>>) :
            SearchPageState<K, V>() {
            override fun toString(): String {
                return "SearchPageState.Page(keyword=${keyword}, flow=${flow})"
            }
        }
    }

    sealed class SearchEvent(val keyword: String) {

        object None : SearchEvent("")
        class Search(keyword: String) : SearchEvent(keyword)
    }

    private val keywordFlow = MutableStateFlow<SearchEvent>(SearchEvent.None)

    var lastPagerState: SearchPageState<Int, Bangumi>? = null

    val pagerFlow = MutableStateFlow<SearchPageState<Int, Bangumi>>(EmptyBangumi)

    init {
        viewModelScope.launch {
            keywordFlow.collectLatest {
                if (it.keyword.isEmpty()) {
                    lastPagerState = EmptyBangumi
                    pagerFlow.emit(EmptyBangumi)
                } else {
                    val n = SearchPageState.Page<Int, Bangumi>(
                        it.keyword,
                        getPager(it.keyword).flow.cachedIn(viewModelScope)
                    )
                    lastPagerState = n
                    pagerFlow.emit(n)
                }
            }
        }
    }

    fun getCurKeyword(): String {
        return keywordFlow.value.keyword
    }

    fun search(keyword: String) {
        viewModelScope.launch {
            keywordFlow.emit(SearchEvent.Search(keyword))
        }
    }

    private fun getPager(keyword: String): Pager<Int, Bangumi> = Pager(
        PagingConfig(pageSize = 10),
        initialKey = searchParser.firstKey()
    ) {
        SearchPageSource(searchParser, keyword)
    }


}

class SearchPageViewModelFactory(
    private val searchParser: ISearchParser,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchPageViewModel::class.java))
            return SearchPageViewModel(searchParser) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}