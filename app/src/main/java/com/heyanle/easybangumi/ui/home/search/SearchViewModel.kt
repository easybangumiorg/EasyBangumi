package com.heyanle.easybangumi.ui.home.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.heyanle.easybangumi.ui.home.AnimSourceFactory
import com.heyanle.easybangumi.ui.home.search.paging.SearchPageSource
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.lib_anim.entity.Bangumi

class SearchViewModel(): ViewModel() {

    val searchTitle = AnimSourceFactory.labelsSearch()

    val controllerList = AnimSourceFactory.searchKeys().map {
        SearchPageController(it, this)
    }

    val keywordState = mutableStateOf("")


    fun getPager(parser: ISearchParser): Pager<Int, Bangumi> = Pager(
        PagingConfig(pageSize = 10),
        initialKey = parser.firstKey()
    ){
        SearchPageSource(parser, keywordState.value?:"")
    }


}