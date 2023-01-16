package com.heyanle.easybangumi.ui.search

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.search.paging.SearchPageSource
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.lib_anim.entity.Bangumi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(): ViewModel() {

    val searchTitle = AnimSourceFactory.labelsSearch()

    val controllerList = AnimSourceFactory.searchKeys().map {
        SearchPageController(it, this)
    }

    val keywordState = mutableStateOf("")

    val searchHistory = mutableStateListOf<String>()

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val history = EasyDB.database.searchHistory.getAllContent()
            searchHistory.clear()
            searchHistory += history
        }
    }

    fun search(keyword: String){
        keywordState.value = keyword
        addHistory(keyword)
    }

    fun addHistory(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            EasyDB.database.searchHistory.insertOrModify(keyword)
        }
        init()
    }


    fun getPager(parser: ISearchParser): Pager<Int, Bangumi> = Pager(
        PagingConfig(pageSize = 10),
        initialKey = parser.firstKey()
    ){
        SearchPageSource(parser, keywordState.value?:"")
    }


}