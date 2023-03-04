package com.heyanle.easybangumi4.ui.sourcehome

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent

/**
 * Created by HeYanLe on 2023/2/25 19:13.
 * https://github.com/heyanLE
 */
class SourceHomeViewModel: ViewModel() {

    // 整个页面需要的数据
    sealed class SourceHomeState {
        object None: SourceHomeState()

        class Normal(val source: Source, val page: List<SourcePage>, val search: SearchComponent?): SourceHomeState()

    }

    var sourceHomeState by mutableStateOf<SourceHomeState>(SourceHomeState.None)
    fun onInit(source: Source, page: List<SourcePage>, search: SearchComponent?){
        Log.d("SourceHomeViewModel", "$source ${page.size} ${search}")
        sourceHomeState = SourceHomeState.Normal(source, page, search)
    }




    // 当前页面
    sealed class CurrentSourcePageState {
        object None: CurrentSourcePageState()

        /**
         * 当前选择某页面
         */
        class Page(val pageIndex: Int): CurrentSourcePageState()

        /**
         * 当前为搜索
         */
        class Search(val keyword: String): CurrentSourcePageState()


    }

    var currentSourceState by mutableStateOf<CurrentSourcePageState>(CurrentSourcePageState.None)


    /**
     * 搜索
     */
    fun search(
        keyword: String
    ){
        withNormal {
            currentSourceState = CurrentSourcePageState.Search(keyword)
        }
    }


    /**
     * 点击 chip
     */
    fun clickPage(
        pageIndex: Int
    ){
        withNormal {
            if(pageIndex < 0 || pageIndex >= it.page.size){
                return
            }
            val page = it.page[pageIndex]
            // 如果页面需要新页面则不处理
            if(!page.newScreen){
                it.page.indexOf(page)
                currentSourceState = CurrentSourcePageState.Page(pageIndex)
            }
        }
    }

    private inline fun withNormal(block: (SourceHomeState.Normal)->Unit){
        sourceHomeState.let {
            if(it is SourceHomeState.Normal){
                block(it)
            }
        }
    }

    private val viewModelOwnerStore = hashMapOf<SourcePage, ViewModelStore>()

    fun getViewModelStoreOwner(page: SourcePage) = object: ViewModelStoreOwner {

        override val viewModelStore: ViewModelStore
            get() {
                var viewModelStore = viewModelOwnerStore[page]
                if (viewModelStore == null) {
                    viewModelStore = ViewModelStore()
                    viewModelOwnerStore[page] = viewModelStore
                }
                return viewModelStore
            }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelOwnerStore.iterator().forEach {
            it.value.clear()
        }
    }





}