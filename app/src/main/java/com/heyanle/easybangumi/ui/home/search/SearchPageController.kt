package com.heyanle.easybangumi.ui.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.home.search.paging.SearchPageSource
import com.heyanle.lib_anim.entity.Bangumi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/10 19:31.
 * https://github.com/heyanLE
 */
class SearchPageController(
    searchKey: String,
    private val vm: ViewModel,
) {

    var isCurLast = false

    object EmptyBangumi : SearchPageState.Empty<Int, Bangumi>()

    sealed class SearchPageState<K : Any, V : Any>{
        open class Empty<K : Any, V : Any>: SearchPageState<K, V>()
        class Page<K : Any, V : Any>(val keyword: String, val flow: Flow<PagingData<V>>): SearchPageState<K, V>()
    }

    private val parser = AnimSourceFactory.requireSearch(searchKey)

    val keywordFlow = MutableStateFlow<String>("")

    val pagerFlow = channelFlow<SearchPageState<Int, Bangumi>> {
        keywordFlow.collectLatest {
            if(it.isEmpty()){
                send(EmptyBangumi)
            }else{
                send(SearchPageState.Page(it, getPager(it).flow.cachedIn(vm.viewModelScope)))
            }
        }
    }

    fun refreshKeyword(keyword: String){
        vm.viewModelScope.launch {
            keywordFlow.emit(keyword)
        }
    }

    fun getPager(keyword: String): Pager<Int, Bangumi> = Pager(
        PagingConfig(pageSize = 10),
        initialKey = parser.firstKey()
    ){
        SearchPageSource(parser, keyword)
    }


}