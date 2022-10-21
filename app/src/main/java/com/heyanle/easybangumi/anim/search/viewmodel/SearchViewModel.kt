package com.heyanle.easybangumi.anim.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.heyanle.easybangumi.anim.search.paging.SearchPageSource
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.lib_anim.ISourceParser
import com.heyanle.lib_anim.entity.Bangumi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * Create by heyanlin on 2022/10/20
 */
class SearchViewModel: ViewModel() {


    private val _keywordLiveData = MutableLiveData<String>("")
    val keywordLiveData: LiveData<String> = _keywordLiveData

    fun submitKeyword(keyword: String){
        _keywordLiveData.value = keyword
    }


    fun getPager(parser: ISearchParser): Pager<Int, Bangumi> = Pager(
        PagingConfig(pageSize = 10),
        initialKey = parser.firstKey()
    ){
        SearchPageSource(parser, keywordLiveData.value?:"")
    }


}