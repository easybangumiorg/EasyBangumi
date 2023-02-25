package com.heyanle.easybangumi4.ui.sourcehome

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.component.page.CartoonPage
import com.heyanle.bangumi_source_api.api2.component.search.SearchComponent

/**
 * Created by HeYanLe on 2023/2/25 19:13.
 * https://github.com/heyanLE
 */
class SourceHomeViewModel: ViewModel() {

    // 整个页面需要的数据
    sealed class SourceHomeState {
        object None: SourceHomeState()

        class Normal(val source: Source, val page: List<CartoonPage>, val search: SearchComponent?): SourceHomeState()

    }

    var sourceHomeState by mutableStateOf<SourceHomeState>(SourceHomeState.None)
    fun onInit(source: Source, page: List<CartoonPage>, search: SearchComponent?){
        Log.d("SourceHomeViewModel", "$source ${page.size} ${search}")
        sourceHomeState = SourceHomeState.Normal(source, page, search)
    }




    // 当前页面
    sealed class CurrentSourcePageState {
        object None: CurrentSourcePageState()

        /**
         * 当前选择某页面
         */
        class Page(val cartoonPage: CartoonPage): CurrentSourcePageState()

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
        page: CartoonPage
    ){
        withNormal {
            // 如果页面需要新页面则不处理
            if(!page.newScreen){
                currentSourceState = CurrentSourcePageState.Page(page)
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

    private val viewModelOwnerStore = hashMapOf<CartoonPage, ViewModelStore>()

    fun getViewModelStoreOwner(page: CartoonPage) = ViewModelStoreOwner {
        var viewModelStore = viewModelOwnerStore[page]
        if (viewModelStore == null) {
            viewModelStore = ViewModelStore()
            viewModelOwnerStore[page] = viewModelStore
        }
        viewModelStore
    }





}