package com.heyanle.easybangumi.ui.search

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/1/17 16:42.
 * https://github.com/heyanLE
 */
class SearchViewModel(
    val default: String = "",
): ViewModel(){

    val keywordState = mutableStateOf("")

    // 搜索事件 flow
    val searchEventState = mutableStateOf("")

    val searchHistory = mutableStateListOf<String>()

    init {
        initSearchHistory()
        keywordState.value = default
        searchEventState.value = default
    }

    fun initSearchHistory() {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) {
                EasyDB.database.searchHistory.getAllContent()
            }
            searchHistory.clear()
            searchHistory += history
        }

    }

    fun search(keyword: String){
        keywordState.value = keyword
        searchEventState.value = keyword
        viewModelScope.launch {
            // 延迟一下
            delay(200)
            addHistory(keyword)
        }

    }

    fun addHistory(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            EasyDB.database.searchHistory.insertOrModify(keyword)
            initSearchHistory()
        }

    }

    fun clearHistory(){
        viewModelScope.launch(Dispatchers.IO) {
            EasyDB.database.searchHistory.deleteAll()
        }
        searchHistory.clear()
    }

}

class SearchViewModelFactory(
    private val default: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java))
            return SearchViewModel(default) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}